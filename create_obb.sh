#!/bin/bash
NODESOURCE="http://nodejs.org/dist/v0.11.14/node-v0.11.14.tar.gz"
JOBB="jobb"
TEMPORARY="/tmp"
CONSODROIDANDROIDDIR=$PWD

CONSODROIDREPOSITORY="https://github.com/hendricha/consoloid-consodroid.git"
CONSODROIDMOCKREPOSITORY="https://github.com/hendricha/consoloid-consodroid-mock.git"

if [[ "$1" == "--help" ]]; then
  echo "Enter help here"
  exit
fi

if [[ "$1" == "--mock" ]]; then
  CONSODROIDREPOSITORY=$CONSODROIDMOCKREPOSITORY
fi

cd $TEMPORARY
git clone $CONSODROIDREPOSITORY consoloid-consodroid
cd consoloid-consodroid
./make_it_deployable.sh

cp /tmp/node .

$JOBB -d . -o $CONSODROIDANDROIDDIR/app/src/main/assets/consodroid.obb -pn hu.hendricha.consodroid -pv 1
echo $RANDOM > $CONSODROIDANDROIDDIR/app/src/main/assets/version

cd $CONSODROIDANDROIDDIR
