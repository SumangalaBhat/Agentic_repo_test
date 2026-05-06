import json
import subprocess

# Load critical issues
with open("critical.json") as f:
    issues = json.load(f)

prompt = f"""
You are a cybersecurity expert.

Generate a professional vulnerability report.

Rules:
- Focus only on CRITICAL issues
- Explain risk clearly
- Provide fix suggestions in code terms
- Format in Markdown

Vulnerabilities:
{json.dumps(issues, indent=2)}
"""

# Call Ollama locally
result = subprocess.run(
    ["ollama", "run", "llama3"],
    input=prompt,
    text=True,
    capture_output=True
)

with open("CRITICAL_REPORT.md", "w") as f:
    f.write(result.stdout)

print("Report generated: CRITICAL_REPORT.md")
