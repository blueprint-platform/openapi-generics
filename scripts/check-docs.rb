# frozen_string_literal: true

require "cgi"
require "nokogiri"
require "kramdown"
require "kramdown-parser-gfm"
require "open3"
require "optparse"
require "pathname"
require "uri"

options = { root: Dir.pwd, site: "_site", baseurl: "/openapi-generics" }
OptionParser.new do |parser|
  parser.on("--root PATH") { |value| options[:root] = value }
  parser.on("--site PATH") { |value| options[:site] = value }
  parser.on("--baseurl PATH") { |value| options[:baseurl] = value }
end.parse!
root = Pathname.new(options[:root]).expand_path
site = Pathname.new(options[:site]).expand_path(root)
baseurl = options[:baseurl]
repository_prefix = "https://github.com/blueprint-platform/openapi-generics/"
errors = []
cache = {}

# Render Markdown instead of regex-parsing links in fenced code examples. Only tracked documents
# are inspected; private local plans/reports and generated build directories are not documentation.
parse = lambda do |path|
  cache[path] ||= begin
    text = path.read
    text = text.sub(/\A---\s*\n.*?\n---\s*\n/m, "") if path.extname.downcase == ".md"
    html = path.extname.downcase == ".md" ? Kramdown::Document.new(text, input: "GFM").to_html : text
    Nokogiri::HTML(html)
  end
end

check = lambda do |from, raw, rendered|
  next if raw.nil? || raw.empty? || raw.start_with?("mailto:", "tel:", "data:", "javascript:")
  # Treat moving own-repository links as local references; leave release-tag and external URLs alone.
  repo_link = raw.start_with?(repository_prefix + "blob/main/", repository_prefix + "tree/main/")
  own_site = raw.start_with?("https://blueprint-platform.github.io/openapi-generics/")
  next if raw.match?(/\A(?:https?:)?\/\//) && !repo_link && !own_site
  url = if repo_link
          raw.sub(/\A#{Regexp.escape(repository_prefix)}(?:blob|tree)\/main\//, "")
        elsif own_site
          raw.sub("https://blueprint-platform.github.io", "")
        else
          raw
        end
  path, fragment = url.split("#", 2)
  path = URI::DEFAULT_PARSER.unescape(path.split("?", 2).first.to_s)
  fragment = URI::DEFAULT_PARSER.unescape(fragment.to_s)
  boundary = repo_link ? root : ((rendered || own_site) ? site : root)
  target = if repo_link
             root.join(path).cleanpath
           elsif rendered || own_site
             if path.start_with?("/")
               unless path == baseurl || path.start_with?(baseurl + "/")
                 errors << "#{from.relative_path_from(root)}: outside site baseurl: #{raw}"
                 next
               end
               site.join(path.delete_prefix(baseurl).delete_prefix("/")).cleanpath
             else
               from.dirname.join(path).cleanpath
             end
           else
             path.empty? ? from : from.dirname.join(path).cleanpath
           end
  target = from if path.empty? && !repo_link
  unless target == boundary || target.to_s.start_with?(boundary.to_s + "/")
    errors << "#{from.relative_path_from(root)}: target escapes documentation root: #{raw}"
    next
  end
  target = target.join("index.html") if rendered && !repo_link && target.directory?
  unless target.exist?
    errors << "#{from.relative_path_from(root)}: missing target: #{raw}"
    next
  end
  if !fragment.empty? && target.file? && %w[.md .html].include?(target.extname.downcase)
    ids = parse.call(target).css("[id]").map { |element| element["id"] }
    errors << "#{from.relative_path_from(root)}: missing fragment ##{fragment}: #{raw}" unless ids.include?(fragment)
  end
end

tracked, status = Open3.capture2("git", "-C", root.to_s, "ls-files", "-z", "--", "*.md")
abort "Cannot list tracked Markdown" unless status.success?
markdown = tracked.split("\0").map { |name| root.join(name) }.select(&:file?)
markdown.each do |path|
  parse.call(path).css("a[href], img[src]").each do |element|
    check.call(path, element["href"] || element["src"], false)
  end
end
abort "Missing rendered site: #{site}" unless site.directory?
html = site.glob("**/*.html")
html.each do |path|
  parse.call(path).css("a[href], img[src], script[src], link[href]").each do |element|
    check.call(path, element["href"] || element["src"], true)
  end
end
abort errors.uniq.join("\n") unless errors.empty?
puts "PASS: #{markdown.size} tracked Markdown documents and #{html.size} rendered pages; internal targets/fragments only."
