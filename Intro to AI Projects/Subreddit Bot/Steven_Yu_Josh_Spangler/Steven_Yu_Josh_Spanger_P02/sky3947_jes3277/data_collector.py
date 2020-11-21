import praw
import random
import re

"""
Name:        data_collector.py
Description: Fills six different folders corresponding to any two subreddits of all 
             possible combinations of the four chosen subreddits of 1000, equally
             split between the two subreddits, formatted posts.
Authors:     Josh Spangler, Steven Yu
"""

reddit = praw.Reddit(client_id='REDACTED', client_secret='REDACTED',
                     password='REDACTED', user_agent='REDACTED',
                     username='REDACTED')

def format_str(postText):
    """ Formats postText by removing all non-alphanumeric characters, except for spaces.
    """
    postText = re.sub(r'([^\s\w]|_)+', ' ', postText)
    postText = postText.replace('  ', ' ')
    postText = postText.strip()
    postText = postText.lower()

    return postText

def save_file(data, folder):
    """ Given a two-dimensional data array and folder name, saves the six unique sets
        of post data (training posts & answers, development posts & answers, etc.) 
        to the given folder.
    """

    # Zip the two subreddit posts together to have alternating
    # training data. This ensures that every slice of 20 training
    # data posts contains an even amount from both subreddits.
    trainArr = list(zip(data[0][:250], data[1][:250]))
    
    # For the development and testing arrays, shuffle them
    devArr   = data[0][250:375] + data[1][250:375]
    testArr  = data[0][375:] + data[1][375:]
    random.shuffle(devArr)
    random.shuffle(testArr)

    trainD = open(folder + "trainD.txt", "w+")
    trainT = open(folder + "trainT.txt", "w+")
    devD   = open(folder + "devD.txt", "w+")
    devT   = open(folder + "devT.txt", "w+")
    testD  = open(folder + "testD.txt", "w+")
    testT  = open(folder + "testT.txt", "w+")

    for i in range(250):
        trainD.write(trainArr[i][0][0])
        trainD.write(trainArr[i][1][0])
        trainT.write(trainArr[i][0][1])
        trainT.write(trainArr[i][1][1])
    for i in range(250):
        devD.write(devArr[i][0])
        devT.write(devArr[i][1])
    for i in range(250):
        testD.write(testArr[i][0])
        testT.write(testArr[i][1])

    trainD.close()
    trainT.close()
    devD.close()
    devT.close()
    testD.close()
    testT.close()


def gen_tsh(data):
    """ Fills the given data array with formatted /r/twosentencehorror posts, sorted from the top of all time.
    """

    # Horror story posts
    i = 0
    for post in reddit.subreddit("twosentencehorror").top('all', limit=750):
        if post.stickied == False and post.edited == False and post.domain == 'self.TwoSentenceHorror' and i < 500:
            if len(post.selftext) > 1:
                while post.selftext[0] == '\n':
                    post.selftext = post.selftext[2:]

            r = post.selftext.find('\n')
            if r > 0:
                postText = post.title + " " + post.selftext[:r]
                postText = format_str(postText)
                
                data.append((postText + "\n", "h\n"))
                i += 1
            else:
                postText = post.title + " " + post.selftext
                postText = format_str(postText)

                data.append((postText + "\n", "h\n"))
                i += 1
    return data


def gen_tss(data):
    """ Fills the given data array with formatted /r/twosentencesadness posts, sorted from the top of all time.
    """
    
    # Sad story posts
    i = 0
    for post in reddit.subreddit("twosentencesadness").top('all', limit=750):
        if post.stickied == False and post.edited == False and post.domain == 'self.TwoSentenceSadness' and i < 500:
            if len(post.selftext) > 1:
                while post.selftext[0] == '\n':
                    post.selftext = post.selftext[2:]

            r = post.selftext.find('\n')
            if r > 0:
                postText = post.title + " " + post.selftext[:r]
                postText = format_str(postText)

                data.append((postText + "\n", "s\n"))
                i += 1
            else:
                postText = post.title + " " + post.selftext
                postText = format_str(postText)

                data.append((postText + "\n", "s\n"))
                i += 1
    return data


def gen_ocp(data):
    """ Fills the given data array with formatted /r/ocpoetry posts, sorted from the top of all time.
    """

    # Poems
    i = 0
    for post in reddit.subreddit("OCPoetry").top('all', limit=1000):
        if post.stickied == False and post.edited == False and post.domain == 'self.OCPoetry' and i < 500:
            txt = format_str(post.selftext)
            txt = txt.replace('\n', ' ')
            txt = re.sub(r'\s+', ' ', txt)
            data.append((txt + "\n", "p\n"))
            i += 1
    return data


def gen_h(data):
    """ Fills the given data array with formatted /r/haiku posts, sorted from the top of all time.
    """

    # Haikus
    i = 0
    for post in reddit.subreddit("haiku").top('all', limit=1000):
        if post.stickied == False and post.edited == False and post.domain == 'self.haiku' and i < 500: 
            txt = format_str(post.title)
            txt = txt.replace('\n', ' ')
            txt = re.sub(r'\s+', ' ', txt)
            data.append((txt + "\n", "j\n"))
            i += 1
    return data


def main():
    """ Fills each of the six folders with its own unique set of testing data.
    """

    folders = ["TSSvTSH/", "OCPvTSH/", "HvTSH/", "OCPvTSS/", "HvTSS/", "HvOCP/"]
    gens = [(gen_tss, gen_tsh), (gen_ocp, gen_tsh), (gen_h, gen_tsh), (gen_ocp, gen_tss), (gen_h, gen_tss), (gen_h, gen_ocp)]

    for i in range(6):
        folder = folders[i]
        gen0 = gens[i][0]
        gen1 = gens[i][1]
        data = [[], []]
        data[0] = gen0(data[0])
        data[1] = gen1(data[1])
        save_file(data, folder)

main()