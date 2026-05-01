import json
import os
import re
import urllib.request
import time

my_exercises = [
    "Bench Press", "Overhead Press", "Incline Dumbbell Press", "Lateral Raises", "Tricep Pushdowns",
    "Deadlift", "Pull Ups", "Lat Pulldown", "Horizontal Pulldown", "T-Bar Rows", "Barbell Rows", "Face Pulls", "Bicep Curls",
    "Squats", "Leg Press", "Romanian Deadlifts", "Leg Extensions", "Calf Raises",
    "Dumbbell Wrist Curls", "Reverse Wrist Curls", "Hammer Curls", "Farmer's Walk",
    "Bodyweight Squats", "Push-Ups", "Lunges", "Plank", "Burpees"
]

def format_res_name(name):
    return "anim_" + re.sub(r'[^a-z0-9]', '_', name.lower()).strip('_')

def compute_similarity(s1, s2):
    s1 = s1.lower().replace('-', ' ')
    s2 = s2.lower().replace('-', ' ')
    words1 = set(s1.split())
    words2 = set(s2.split())
    if not words1: return 0
    return len(words1.intersection(words2)) / float(max(len(words1), 1))

with open('exercises_data.json', 'r') as f:
    data = json.load(f)

matches = {}
for my_ex in my_exercises:
    best_match = None
    best_score = 0
    for ex in data:
        score = compute_similarity(my_ex, ex['name'])
        if score > best_score:
            best_score = score
            best_match = ex
    
    if best_match and best_score > 0:
        matches[my_ex] = best_match

kotlin_map = "val exerciseAnimations = mapOf(\n"
for my_ex, ex_data in matches.items():
    gif_url_rel = ex_data.get('gif_url')
    if not gif_url_rel: continue
    
    res_name = format_res_name(my_ex)
    url = f"https://raw.githubusercontent.com/hasaneyldrm/exercises-dataset/main/{gif_url_rel}"
    file_path = f"app/src/main/res/drawable/{res_name}.gif"
    
    print(f"Downloading {url} to {file_path}")
    try:
        urllib.request.urlretrieve(url, file_path)
    except Exception as e:
        print(f"Failed to download {my_ex}: {e}")
        continue
    
    kotlin_map += f'    "{my_ex}" to R.drawable.{res_name},\n'

kotlin_map += ")\n"
with open("kotlin_map.txt", "w") as f:
    f.write(kotlin_map)
