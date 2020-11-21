"""
File:        classifiers.py
Description: predicts classification data for all combinations of two subreddits (of the four we chose),
             writes the results to a file for later analysis, then prints the learning curve graphs for each.
Authors:     Josh Spangler, Steven Yu
"""

from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.svm import SVC
import matplotlib.pyplot as plt 

def main():
    """ From all four chosen subreddits, this file, using the two classifiers (SVC and RFC), writes 
        classification result data of any two subreddits equally split in a set of data set files."""

    # Initial stuff
    folders = ["TSSvTSH/", "OCPvTSH/", "HvTSH/", "OCPvTSS/", "HvTSS/", "HvOCP/"]
    outF = open("out.txt", "w+")
    iterationsData = { "TSSvTSH" : ([50], [50]), "OCPvTSH" : ([50], [50]), "HvTSH" : ([50], [50]), "OCPvTSS" : ([50], [50]),
                       "HvTSS": ([50], [50]), "HvOCP" : ([50], [50])}
    count_vect = CountVectorizer()
    tfidf_transformer = TfidfTransformer()
    clf_svc = SVC(gamma='scale')
    clf_rfc = RandomForestClassifier(n_estimators=100)

    # Loop through all dual subreddit folders, testing each possible data set
    for folder in folders:
        # Load training data for particular file 
        trainD = open(folder + "trainD.txt").read().split('\n')
        trainT = open(folder + "trainT.txt").read().split('\n')
        devD   = open(folder + "devD.txt").read().split('\n')
        devT   = open(folder + "devT.txt").read().split('\n')
        testD  = open(folder + "testD.txt").read().split('\n')
        testT  = open(folder + "testT.txt").read().split('\n')
        # Grab sets of 20 training data posts
        for i in range(0, 500, 20):
            outF.write(folder[:-1] + ", iteration " + str(i/20) + ":\n")

            # Get next chunk of training set
            trainDChunk = trainD[:i+20]
            trainTChunk = trainT[:i+20]
            
            # Transform datasets into feature vectors
            train_counts = count_vect.fit_transform(trainDChunk)
            train_tfidf = tfidf_transformer.fit_transform(train_counts)
            dev_counts = count_vect.transform(devD)
            dev_tfidf = tfidf_transformer.transform(dev_counts)
            test_counts = count_vect.transform(testD)
            test_tfidf = tfidf_transformer.transform(test_counts)
            
            # Train SVC and RFC
            clf_svc.fit(train_tfidf, trainTChunk)
            clf_rfc.fit(train_tfidf, trainTChunk)

            # Write Dev tests results to file
            outF.write("Dev. set results:\n")
            res_svc = round(clf_svc.score(dev_tfidf, devT)*100, 2)
            res_rfc = round(clf_rfc.score(dev_tfidf, devT)*100, 2)
            outF.write("SVC: " + str(res_svc) + "%, RFC: " + str(res_rfc) + "%\n")

            # Write Test tests results to file
            outF.write("Test set results:\n")
            res_svc = round(clf_svc.score(test_tfidf, testT)*100, 2)
            res_rfc = round(clf_rfc.score(test_tfidf, testT)*100, 2)
            outF.write("SVC: " + str(res_svc) + "%, RFC: " + str(res_rfc) + "%\n\n")
            iterationsData[folder[:-1]][0].append(res_svc)
            iterationsData[folder[:-1]][1].append(res_rfc)
            
            outF.write("------------------------------\n")
        
        outF.write("==============================\n")

    outF.close()

    # Make graphs
    for folder in folders:
        plt.suptitle("Learning Curve for " + folder[:-1])
        plt.plot(iterationsData[folder[:-1]][0], label="SVC")
        plt.plot(iterationsData[folder[:-1]][1], label="RFC")
        plt.xlabel("Training set size (multiple of 20, 500 in total)")
        plt.ylabel("% Accuracy")
        plt.xlim([0, 25])
        plt.ylim([0,100])
        plt.legend()
        plt.figure()

    plt.show()

main()
