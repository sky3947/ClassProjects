import argparse
import sys
import pdb
import time
import math
import gym
from gym import wrappers, logger
from enum import Enum

# MAX SCORE: 1480

# Pixel constants
SPACESHIP  = [240, 128, 128]
BULLET     = [117, 181, 239]
SPACE      = [0, 0, 0]

# Dimension constants
WIDTH  = 160
HEIGHT = 210
SCOREBOARD_HEIGHT = 14

# Agent constants
MIN_RADIUS = 35
START_LIVES = 3
TURN_FRAME_RIGHTMOST = 7.5
TURN_FRAME_LEFTMOST = -7.5
TURN_FRAME_LEFTMAX = -30
TURN_FRAME_RIGHTMAX = 30
FIRE_COOLDOWN = 4
LEFTMOST_ANGLE = 180
FRAME_UNIT = 0.5

# Action meanings
NOOP = 0
FIRE = 1
UP = 2
RIGHT = 3
LEFT = 4

# Agent states
class States(Enum):
    RUN = 0
    GUN = 1


class Agent(object):
    """The 'Mobile' Agent"""
    def __init__(self, action_space):
        self.loc           = (WIDTH/2, HEIGHT/2)     # Default start in middle
        self.radius        = MIN_RADIUS              # "Personal space" agent radius
        self.lives         = START_LIVES
        self.turn_frames   = 0                       # Current frame count of ship
        self.fire_cooldown = 0                       # Agent firing "cooldown"
                                                     # Want to fire every other frame
        self.state         = States.GUN              # Start in gun state
        self.min_asteroid  = (None, float('inf'))    # Current min-asteroid 
        self.asteroids_pts = []                      # Current known asteroid pts
        self.action_space  = action_space
        

    # You should modify this function
    def act(self, observation, reward, done, lives):
        return self.observe(observation, lives)


    def observe(self, observation, lives):
        """Parse through the r,g,b space environment and buildup an
        array of asteroid centroid points with its relative distance 
        from the spaceship
        """
        
        # First detect if we died, if so reset values
        if self.lives != lives:
            self.reset(lives)

        # Parse through the raw 3D environment, build up space dict for
        # easy (x, y) -> [r,g,b] access later
        x = y = 0
        space = {}              # Format: { (x, y) : [r, g, b] }  
        for m2d in observation:
            for m in m2d:
                # Only start storing values when we are past the scoreboard
                if y > SCOREBOARD_HEIGHT:
                    space[ (x,y) ] = m.tolist()
                x += 1
            y += 1
            x = 0

        # Parse through space dict to detect spaceship and asteroids
        spaceship_pts = []      # [ (x1, y1), (x2, y2), ... ]
        asteroids_pts = []      # [ [(a1x1, a1y1), ...], [(a2x1, a2y1), ...], ...]
        for (x, y) in space:
            p = space[ (x, y) ]
            # Upon finding some asteroid 
            if self.is_asteroid(p):
                # Only add asteroid if its coordinate is not already in the array
                cond = True
                for a_pts in asteroids_pts:
                    if (x, y) in a_pts:
                        cond = False
                if cond:
                    asteroids_pts.append(self.build_asteroid_pts( (x,y), space ))
            # Upon finding our spaceship
            elif p == SPACESHIP:
                spaceship_pts.append( (x, y) )
            
        # Update agent location and points if necessary
        if spaceship_pts:
            self.loc = self.get_centroid(spaceship_pts)

        # Buildup asteroid centroid points for each detected asteroid 
        asteroids_centroids = []        # [ac1, ac2, ..., acN]
        for a_pts in asteroids_pts:
            asteroids_centroids.append(self.get_centroid(a_pts))

        # Update asteroids_pts in agent in necessary
        if asteroids_centroids:
            self.asteroids_pts = asteroids_centroids

        # Buildup distances array (spaceship to asteroid)
        distances = {}                  # [ ac1 : d1, ac2 : d2, ...]
        for ac in asteroids_centroids:
            distances[ac] = self.get_distance(ac, self.loc)

        # Find minimum asteroid to spaceship distance,
        # and update it if needed
        min_a = (None, float('inf'))
        for (ac, d) in distances.items():
            if d < min_a[1]:
                min_a = (ac, d)
        
        # Set new minimum asteroid is newly found one isn't None
        if min_a[0] != None:
            self.min_asteroid = min_a

        # Decide what to do knowing the minimum-distance asteroid coordinates
        return self.decide()


    def decide(self):
        """ After parsing the observable environment, the agent now
        can decide what actions to take to survive
        """

        # The estimated ship angle using its current turn_frames
        ship_estimate_ang  = self.estimate_ship_angle()

        # Safety is top priority - see if our personal space is being compromised
        # If so, RUN from nearest asteroid
        if self.min_asteroid[1] <= self.radius:
            self.state = States.RUN
            x_difference = self.min_asteroid[0][0] - self.loc[0]

            # Convert ship angle to positive value to detect which direction to turn
            if ship_estimate_ang < 0:
                ship_estimate_ang = 360 + ship_estimate_ang

            # If we need to move right
            if x_difference < 0:
                if self.turn_frames != TURN_FRAME_RIGHTMOST:
                    # If the ship angle is less than 180, turn right
                    if ship_estimate_ang < LEFTMOST_ANGLE:
                        self.turn_frames += FRAME_UNIT
                        self.update_turn_frames()
                        return RIGHT
                    # If the ship angle is greater than 180, turn left
                    else:
                        self.turn_frames -= FRAME_UNIT
                        self.update_turn_frames()
                        return LEFT
                else:
                    return UP
            # If we need to move left
            else:
                if self.turn_frames != TURN_FRAME_LEFTMOST:
                    # If the ship angle less than 180, turn left
                    if ship_estimate_ang < LEFTMOST_ANGLE:
                        self.turn_frames -= FRAME_UNIT
                        self.update_turn_frames()
                        return LEFT
                    # If the ship angle is greater than 180, turn right
                    else:
                        self.turn_frames += FRAME_UNIT
                        self.update_turn_frames()
                        return RIGHT
                else:
                    return UP
                

        # If no asteroid is close enough to evade, enter combat mode
        self.state = States.GUN
        res = self.shoot()

        # If we can shoot at this point
        if res:
            return FIRE
        # If firing is on cooldown, turn towards the closest asteroid
        else:
            ship_to_asteroid = self.get_angle(self.loc, self.min_asteroid[0])
            target_frame = self.estimate_turn_frame(ship_to_asteroid)

            if self.turn_frames != target_frame:
                ang_difference = ship_estimate_ang - ship_to_asteroid
                # If the ship angle is above the asteroid to ship angle
                if ang_difference > 0:
                    self.turn_frames += FRAME_UNIT
                    self.update_turn_frames()
                    return RIGHT
                else:
                    self.turn_frames -= FRAME_UNIT
                    self.update_turn_frames()
                    return LEFT
            # Shoot here because we are already at target frame
            return FIRE
        

    def build_asteroid_pts(self, s, space):
        """ Using a source asteroid (x, y) coordinate, builds up an array of (x, y)
        asteroid coordinates that all makeup a single asteroid using BFS.
        """

        # Initial data
        queue = [s]
        visited = [s]
        asteroid_pts = []

        while queue:
            p  = queue.pop(0)
            xp = space[ (p[0], p[1]) ]    # Get pixel arr for specific point
            
            # See if we have an asteroid pixel
            if self.is_asteroid(xp):
                asteroid_pts.append( (p[0], p[1]) )

            # Add possible neighbors (have to be asteroid pixels as well)
            x = p[0]
            y = p[1]
            # Right
            if x+1 != WIDTH and (x+1, y) not in visited and self.is_asteroid(space[ (x+1, y) ]):   
                queue.append( (x+1, y) )
                visited.append( (x+1, y) )
            # Left
            if x != 0 and (x-1, y) not in visited and self.is_asteroid(space[ (x-1, y) ]):         
                queue.append( (x-1, y) )
                visited.append( (x-1, y) )
            # Below
            if y+1 != HEIGHT and (x, y+1) not in visited and self.is_asteroid(space[ (x, y+1) ]): 
                queue.append( (x, y+1) )
                visited.append( (x, y+1) )
            # Above
            if y != 0 and (x, y-1) not in visited and self.is_asteroid(space[ (x, y-1) ]):         
                queue.append( (x, y-1) )
                visited.append( (x, y-1) )
                
        return asteroid_pts


    def shoot(self):
        """ Decide when to shoot based on a two frame cooldown
        """

        # Fire only on every other frame, otherwise turn twoards the closest asteroid
        self.fire_cooldown += 1
        if self.fire_cooldown > FIRE_COOLDOWN:
            self.fire_cooldown = 0
            return 1
        # Otherwise, do nothing
        return 0


    def is_asteroid(self, p):
        """Checks to see if a given pixel [r,g,b] array is an asteroid
        pixel or not
        """
        return (p != SPACE and p != SPACESHIP and p != BULLET)


    def get_centroid(self, arr):
        """Returns a centroid point of a given array of points.
        This array of points may be that of a spaceship or asteroid
        """
        xs = ys = c = 0
        for (x, y) in arr:
            xs += x
            ys += y
            c  += 1
        
        return (xs/c, ys/c)

    
    def get_distance(self, p1, p2):
        """Calculates the distance between two points. Will be
        Used to calculate distances from spaceship to asteroid
        """
        return math.sqrt((p2[0]-p1[0])**2 + (p2[1]-p1[1])**2) 


    def estimate_ship_angle(self):
        """ Estimates the ship's angle in degrees by taking into account
        its current frame number. Note that (+) frames denote
        a right motion and (-) frames in left motion
        """
        return 90 - (12 * self.turn_frames)

    
    def estimate_turn_frame(self, angle):
        """ Estimates the ship's turn frame by taking into account
        a certain angle. Is used to estimate how many frames we need
        to turn to be aiming at an asteroid. This is simply the inverse
        of the function used in estimate_ship_angle()
        """
        return (90 - angle) // 12


    def update_turn_frames(self):
        """ Updates the agent's turn frames so that they are always in
        modspace [-30, 30]
        """
        if self.turn_frames == TURN_FRAME_LEFTMAX or self.turn_frames == TURN_FRAME_RIGHTMAX:
            self.turn_frames = 0
    
      
    def get_angle(self, p1, p2):
        """ Calculates the angle between two points via the
        tan inverse of the point's slope. Used in running
        away from asteroids
        """
        return math.degrees(math.atan2(p2[1]-p1[1], p2[0]-p1[0]))*-1    # ???


    def reset(self, new_lives):
        """ Resets the location, min_asteroid agent and lives
        values upon. Used to reset upon death
        """
        self.loc = (WIDTH/2, HEIGHT/2)
        self.turn_frames  = 0
        self.lives = new_lives


## YOU MAY NOT MODIFY ANYTHING BELOW THIS LINE OR USE
## ANOTHER MAIN PROGRAM
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=None)
    parser.add_argument('--env_id', nargs='?', default='AsteroidsNoFrameskip-v4', help='Select the environment to run')
    args = parser.parse_args()

    # You can set the level to logger.DEBUG or logger.WARN if you
    # want to change the amount of output.
    logger.set_level(logger.INFO)

    env = gym.make(args.env_id)

    # You provide the directory to write to (can be an existing
    # directory, including one with existing data -- all monitor files
    # will be namespaced). You can also dump to a tempdir if you'd
    # like: tempfile.mkdtemp().
    outdir = 'random-agent-results'

    env.seed(0)
    agent = Agent(env.action_space)
   

    episode_count = 100
    reward = 0
    done = False
    score = 0
    special_data = {}
    special_data['ale.lives'] = 3
    lives = START_LIVES
    ob = env.reset()
    while not done:
        
        action = agent.act(ob, reward, done, lives)
        ob, reward, done, x = env.step(action)
        lives = x
        #pdb.set_trace()
        score += reward
        env.render()
     
    # Close the env and write monitor result info to disk
    print ("Your score: %d" % score)
    env.close()
