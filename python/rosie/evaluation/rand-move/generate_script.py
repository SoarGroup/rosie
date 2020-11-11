import random
import sys

object_ids = dict( (name, i+1) for i, name in enumerate(["fridge", "sink", "counter", "table", "pantry", "garbage", "apple", "soda", "mug", "fork", "water", "ketchup" ]) )

# 1 argument (optional) - length of script (number of moves to do)
NUM_MOVES = 10 if len(sys.argv) == 1 else int(sys.argv[1])

objects = [ "apple", "soda", "mug", "water", "fork", "ketchup" ]
locations = [ "fridge", "sink", "counter", "table", "pantry", "garbage" ]

surfaces = [ "counter", "table" ]
receptacles = [ "fridge", "sink", "pantry", "garbage" ]
doors = ["fridge", "pantry"]

obj_positions = { "apple": "table", "soda": "table", 
            "fork": "counter", "mug": "counter",
            "ketchup": "fridge", "water": "fridge" }

def get_rand_move():
    obj = random.choice(objects)
    loc = random.choice(locations)
    while obj_positions[obj] == loc:
        loc = random.choice(locations)
    prep = "into" if loc in receptacles else "onto"
    return (obj, prep, loc)


sentences = []
for i in range(NUM_MOVES):
    # Generate a random move command
    obj, prep, loc = get_rand_move()
    sentences.append("Move the {} {} the {}.".format(obj, prep, loc))
    if i == 0:
        # First time only -- give the goal
        sentences.append("The only goal is that the {} is {} the {}.".format(obj, prep[0:2], loc))
    obj_positions[obj] = loc

    # Randomly relocate one of the objects
    obj, rel, loc = get_rand_move()
    rel = rel[0:2] + "1"
    sentences.append("!CMD place {} {} {}".format(object_ids[obj], rel, object_ids[loc]))
    obj_positions[obj] = loc

    # With a 33% probability, close one the of the doors
    if random.randint(0, 3) == 0: 
        door = random.choice(doors)
        sentences.append("!CMD close {}".format(object_ids[door]))

with open("rand-move.script", 'w') as f:
    f.write("\n".join(sentences))

