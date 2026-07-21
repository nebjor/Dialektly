#!/bin/bash
REPO="PM2-IT25taWIN-muon-welj-bira/team03-nullpointerninjas-projekt2-dialektly"
MS="Sprint 1 - Foundation & Domain"

GH_HOST=github.zhaw.ch gh issue create --repo $REPO --title "Create mocks for Vocis" --label "priority:high,sprint-1" --milestone "$MS" --body "Implement mocks for Vocis."
GH_HOST=github.zhaw.ch gh issue create --repo $REPO --title "Define JSON files (tempDir) for Mockito tests" --label "priority:high,sprint-1" --milestone "$MS" --body "Plan how JSON files in tempDir should be structured for Mockito tests."
GH_HOST=github.zhaw.ch gh issue create --repo $REPO --title "Review test concept and report corrections" --label "priority:high,sprint-1" --milestone "$MS" --body "Read the test concept, identify missing or incorrect parts. Test scope is not up to date yet."
GH_HOST=github.zhaw.ch gh issue create --repo $REPO --title "UML: Add getSessionSummary() to LearningSession" --label "priority:high,sprint-1" --milestone "$MS" --body "getSessionSummary() is missing in the UML. The method counts correct and incorrect exercises."
GH_HOST=github.zhaw.ch gh issue create --repo $REPO --title "Implement simple learning round (10 questions, no repetitions)" --label "priority:high,sprint-1" --milestone "$MS" --body "Simple version: 10 questions per round, no repetitions. Write mastered vocabularies directly to the master list using the mastered flag."
GH_HOST=github.zhaw.ch gh issue create --repo $REPO --title "Implement CreateSession method" --label "priority:high,sprint-1" --milestone "$MS" --body "CreateSession is missing. The method creates a new session with 10 exercises and is called by the SessionPresenter."
GH_HOST=github.zhaw.ch gh issue create --repo $REPO --title "Implement LearningSession as Observable" --label "priority:high,sprint-1" --milestone "$MS" --body "LearningSession should be Observable. SessionPresenter registers as Observer. On submit, onSubmitAnswer() is forwarded to the presenter."
GH_HOST=github.zhaw.ch gh issue create --repo $REPO --title "Show Correct/False flag on Exercise after evaluation" --label "priority:high,sprint-1" --milestone "$MS" --body "Exercise gets a Correct/False flag after evaluation. Set by the EventHandler in the model."
GH_HOST=github.zhaw.ch gh issue create --repo $REPO --title "Pass only required AppState parts to classes" --label "priority:high,sprint-1" --milestone "$MS" --body "Classes that only need part of the AppState should only receive that part, not the entire AppState object."
echo "✅ All issues created!"
