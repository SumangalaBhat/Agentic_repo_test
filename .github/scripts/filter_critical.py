import json

with open("semgrep.json") as f:
    data = json.load(f)

critical = []

for r in data.get("results", []):
    if r.get("extra", {}).get("severity", "").upper() == "ERROR":
        critical.append({
            "file": r["path"],
            "line": r["start"]["line"],
            "message": r["extra"]["message"],
            "rule": r["check_id"]
        })

with open("critical.json", "w") as f:
    json.dump(critical, f, indent=2)

print(f"Critical vulnerabilities found: {len(critical)}")
