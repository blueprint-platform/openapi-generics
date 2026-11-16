"""Assert the tutorial's actual generated binding and compiled API-use probe, not an HTTP claim."""
import pathlib
import re
import sys

root = pathlib.Path(sys.argv[1])
for tool, output, classes in [
    ("maven", "target/generated-sources/openapi/src/gen/java", "target/classes"),
    ("gradle", "build/generated/openapi/src/gen/java", "build/classes/java/main"),
]:
    project = root / tool
    model = project / output / "example/generated/model/ServiceResponseGreeting.java"
    assert re.search(r"extends\s+ServiceResponse\s*<Greeting>", model.read_text()), model
    for name in ["example/UseClient", "example/generated/api/GreetingApi", "example/generated/model/ServiceResponseGreeting"]:
        assert (project / classes / (name + ".class")).is_file(), (tool, name)
    assert not (project / output / "example/generated/model/Meta.java").exists()
    print(f"PASS: {tool} public 1.3.0 generation, generic binding and API-use compilation")
