#!/bin/bash
#sudo apt-get install libbz2-dev
NODEVERSION="v0.11.14"
NODESOURCE="http://nodejs.org/dist/$NODEVERSION/node-$NODEVERSION.tar.gz"
NDKVERSION="r10b"
NDKSOURCE="http://dl.google.com/android/ndk/android-ndk32-$NDKVERSION-linux-x86_64.tar.bz2"
JOBB="jobb"
TEMPORARY="/tmp"
CONSODROIDANDROIDDIR=$PWD

CONSODROIDREPOSITORY="https://github.com/hendricha/consoloid-consodroid.git"
CONSODROIDMOCKREPOSITORY="https://github.com/hendricha/consoloid-consodroid-mock.git"

if [[ "$1" == "--help" ]]; then
  cat creat_obb.md
  exit
fi

NDKDIR=$1

if [[ "$1" == "--mock" ]]; then
  CONSODROIDREPOSITORY=$CONSODROIDMOCKREPOSITORY
  NDKDIR=$2
fi

if [[ "$1" == "--use-local-dir" ]]; then
  CONSODROIDDIR=$PWD/consoloid-consodroid
  NDKDIR=$2
else
  cd $TEMPORARY

  git clone $CONSODROIDREPOSITORY consoloid-consodroid
  cd consoloid-consodroid
  ./make_it_deployable.sh
  rm -rf node_modules/fs-ext
  rm -rf node_modules/contextify
  cd node_modules
  git clone https://github.com/hendricha/contextify.git
  rm -rf contextify/.git
  cd contextify
  npm install nan bindings

  CONSODROIDDIR=$TEMPORARY/consoloid-consodroid
fi

if [[ "$1" == "--download-ndk" ]] || [[ "$2" == "--download-ndk" ]]; then
  cd $TEMPORARY
  wget --output-document ndk.tar.bz2 $NDKSOURCE
  tar jxvf ndk.tar.bz2
  NDKDIR=$TEMPORARY/android-ndk-$NDKVERSION
fi

if [[ "$1" == "--use-local-dir" ]]; then
  NODEDIR=$PWD/node-source
else
  cd $TEMPORARY
  wget --output-document node.tar.gz $NODESOURCE
  tar xvf node.tar.gz
  NODEDIR=$TEMPORARY/node-$NODEVERSION
fi

cd $NODEDIR

sed 's/4.7/4.8/g' android-configure > android-configure4.8
chmod +x android-configure4.8
. ./android-configure4.8 $NDKDIR
export PATH=/usr/bin:$PATH
make

cd $CONSODROIDDIR/node_modules/contextify
cd build
make
cp $NODEDIR/out/Release/node $CONSODROIDDIR

cd $CONSODROIDDIR
rm -f $CONSODROIDANDROIDDIR/app/src/main/assets/consodroid.obb
$JOBB -d . -o $CONSODROIDANDROIDDIR/app/src/main/assets/consodroid.obb -pn hu.hendricha.consodroid -pv 1
echo $RANDOM > $CONSODROIDANDROIDDIR/app/src/main/assets/version

cd $CONSODROIDANDROIDDIR
