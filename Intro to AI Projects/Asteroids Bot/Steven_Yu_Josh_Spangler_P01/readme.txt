1) To run the code, put the two provided agent files (gym-play-mobile.py,
   and gym-play-stat.py) into the top-level "gym/" folder provided by the
   openai/gym github.

2) Note that both of our agents are ran with the parser argument
   "AsteroidsNoFrameskip-v4" instead of the default "Asteroids-v0" option.

3) Note that, as confirmed it was okay to do in class, we saved and passed
   the 'x' (lives) variable returned by the env.step(action) call in the main
   while loop into our agent.act functions (for both agents)

4) Note that we used the default seed across all tests and agents:
   "env.seed(0)"
