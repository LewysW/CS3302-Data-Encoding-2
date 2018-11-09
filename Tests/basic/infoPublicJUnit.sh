#!/bin/bash
JUNITPATH=$TESTDIR/../lib

java -cp $TESTDIR:$JUNITPATH/junit-platform-console-standalone.jar:. org.junit.platform.console.ConsoleLauncher --disable-ansi-colors --details=tree -c=ECCPublicTests | grep --invert-match -e 'at [osj][rua][gnv].[jr][ue][nf][il][te]'
