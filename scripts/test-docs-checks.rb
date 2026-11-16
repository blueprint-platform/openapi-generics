# frozen_string_literal: true

require "minitest/autorun"
require "tmpdir"
require "fileutils"
require "open3"
require "rbconfig"

class DocsChecksTest < Minitest::Test
  def setup
    @root = Dir.mktmpdir("docs-link-check-")
    FileUtils.mkdir_p(["#{@root}/docs", "#{@root}/_site"])
    File.write("#{@root}/README.md", "[Guide](docs/page.md#source-heading)\n")
    File.write("#{@root}/docs/page.md", "# Source heading\n[External](https://unreachable.invalid/never-requested)\n")
    File.write("#{@root}/_site/index.html", '<a href="/openapi-generics/page.html#rendered-heading">Guide</a>')
    File.write("#{@root}/_site/page.html", '<h1 id="rendered-heading">Rendered heading</h1>')
    system("git", "-C", @root, "init", "-q", exception: true)
    system("git", "-C", @root, "add", "README.md", "docs", exception: true)
  end

  def teardown
    FileUtils.remove_entry(@root)
  end

  def check
    Open3.capture3(RbConfig.ruby, File.expand_path("check-docs.rb", __dir__), "--root", @root)
  end

  def test_valid_local_links_and_external_urls
    output, error, status = check
    assert status.success?, output + error
  end

  def test_missing_source_target_fails
    File.write("#{@root}/README.md", "[Missing](docs/absent.md)\n")
    _, error, status = check
    refute status.success?
    assert_includes error, "missing target"
  end

  def test_missing_source_fragment_fails
    File.write("#{@root}/README.md", "[Missing](docs/page.md#absent)\n")
    _, error, status = check
    refute status.success?
    assert_includes error, "missing fragment"
  end

  def test_missing_rendered_fragment_fails
    File.write("#{@root}/_site/page.html", '<h1 id="renamed">Renamed</h1>')
    _, error, status = check
    refute status.success?
    assert_includes error, "missing fragment"
  end
end
