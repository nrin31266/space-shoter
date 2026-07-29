import json

with open("assets/data/waves.json", "r") as f:
    data = json.load(f)

for wave in data["waves"]:
    for action in wave["actions"]:
        if "hoverYPct" in action and action["hoverYPct"] > 0.6:
            action["hoverYPct"] = round(action["hoverYPct"] - 0.2, 2)

with open("assets/data/waves.json", "w") as f:
    json.dump(data, f, indent=2)

