#!/bin/bash

# make sure it is using bash
# /usr/bin/env bash

# check java version is at least 1.5
JAVA_LOC=`which java`
JAVA_REQUIRED=1.5
JAVA_REQUIRED=`echo $JAVA_REQUIRED | sed -e 's;\.;0;g'`
if [ $JAVA_LOC ]
then
	VER=`java -version 2>&1 | grep "java version" | awk '{print $3}' | tr -d \" `
	echo "Using Java at " $JAVA_LOC $VER
	VER=`echo $VER | awk '{ print substr($1, 1, 3); }' | sed -e 's;\.;0;g'`
	# echo "Using major version " $VER "required is" $JAVA_REQUIRED
	if [ $VER -lt $JAVA_REQUIRED ]; then
	# if (( $(echo "$VER < $JAVA_REQUIRED" | awk '{print ($1 < $2)}') )); then
		echo "Recommended Java version is 1.6 or higher"
	fi
else
	echo "Could not find Java in the path, please install Java"
	exit 0
fi

# is ant installed? Recommended is 1.8 but at least 1.7
ANT_LOC=`which ant`
ANT_REQUIRED=1.7
ANT_BEST=1.8
ANT_REQUIRED=`echo $ANT_REQUIRED | sed -e 's;\.;0;g'`
ANT_BEST=`echo $ANT_BEST | sed -e 's;\.;0;g'`
if [ $ANT_LOC ]
then
	VER=`ant -version 2>&1 | grep "Ant version" | awk '{print $4}' | tr -d \" `
	echo "Using ant at " $ANT_LOC $VER
	VER=`echo $VER | awk '{ print substr($1, 1, 3); }' | sed -e 's;\.;0;g'`
	# echo "$VER < $ANT_REQUIRED"
	if [ $VER -lt $ANT_REQUIRED ]; then
	# if (( $(echo "$VER < $ANT_REQUIRED" | awk '{print ($1 < $2)}') )); then
		echo "Ant version required is 1.7 or higher"
	fi
	if [ $VER -lt $ANT_BEST ]; then
	# if (( $(echo "$VER < $ANT_REQUIRED" | awk '{print ($1 < $2)}') )); then
		echo "Recommended ant version is 1.8 or higher"
	fi
else
	echo "Could not find ant in path, please install ant"
	exit 0
fi

# check the various lmtools components

missing=0
missingmessage=0

checklmversion(){
	program=$1
	required="V2.05"
	location=`which $program`

	if [ $location ]; then
		version=`$program -version 2>&1`
		ver=`$program -version 2>&1 | awk '{print $7}' | tr -d \" `
		# echo $program "found" $version "ver" $ver "requires" $required
		if [ $ver == $required ]; then
			echo "Using " $version
			return
		else
			echo $program "requires" $required
			missing=1
		fi
	else
		if [ $missingmessage -eq 0 ]; then
			echo "You need the Edinburgh/CMU tools to create lanaugage models."
			echo "This includes several programs. These tools can be found at"
			echo "http://www.speech.cs.cmu.edu/SLM/toolkit.htm"
			missingmessage=1
		fi
		missing=1
	fi
}

# check the language modeling kit versions
checklmversion "text2wfreq"
checklmversion "wfreq2vocab"
checklmversion "text2idngram"
checklmversion "idngram2lm"
if [ $missing -eq 1 ]; then
	echo "install CMU LM kit 2.05 before proceeding"
	exit 0
fi

# check LD_LIBRARY_PATH, should contain the sphinx libraries.
# should do this like classpath, expanding each component and checking
# for the / end marker

libinpath=0

checkforlib(){
	file=$1
	lines=`echo $LD_LIBRARY_PATH | awk -F: '{ for(j=1;j<=NF;j++) print $j;}'`
	for token in $lines; do
		file=$token$check
		if [ -f $file ]; then
			echo "LD_LIBRARY_PATH contains" $file "in directory" $token
			libinpath=1
			break
		fi
	done
}

s3libcheck="libsphinxbase.a"

if [ $LD_LIBRARY_PATH ]; then
	checkfor $s3libcheck
	if [ $libinpath -eq 0 ]; then
		echo "sphinxbase libraries are not in LD_LIBRARY_PATH"
		exit 0
	else
		echo "sphinxbase library is available"
	fi
else
	echo "Depending on your linux version, it may be necessary to export LD_LIBRARY_PATH"
	echo "     to include sphinxbase libraries, it is probably in /usr/local/lib"
	# exit 0
fi

# check whether sphinx_lm_convert is installed from sphinxbase
LM_CONVERT=`which sphinx_lm_convert`
if [ $LM_CONVERT ]; then
	echo "Using " $LM_CONVERT
else
	echo "Missing sphinx_lm_convert, please install sphinxbase"
	exit 0
fi

# check and add to classpath

# check whether a representative file is already in the classpath

fileinclasspath=0
classpath=$CLASSPATH

checkforfile(){
	file=$1
	package=$2
	lines=`echo $CLASSPATH | awk -F: '{ for(j=1;j<=NF;j++) print $j;}'`
	for token in $lines; do
		file=$token$check
		if [ -f $file ]; then
			echo $2 "expanded and contains" $file "in classpath element" $token
			fileinclasspath=1
			break
		fi
	done
}

checkcontainsdot(){
	file="."
	check=`echo $CLASSPATH | grep "\." `
	# lines=`echo $CLASSPATH | awk -F: '{ for(j=1;j<=NF;j++) print $j;}'`
	# for token in $lines; do
	if [ $check ]; then
		echo "CLASSPATH contains current directory" $token
		fileinclasspath=1
		break
	else
		if [ $classpath ]; then
			classpath=".":$classpath
		else
			classpath="."
		fi
		echo "CLASSPATH modified to contain current directory"
		echo "CLASSPATH now is " $classpath
	fi
	# done
}

maybedir=""

modifyclasspath(){
	TOCHECK=$1
	CHECK=` echo $classpath | grep "$TOCHECK" `
	if [ $CHECK ]
	then
		echo $TOCHECK "is in the classpath"
	else
		if [ $maybedir ]; then
			test=` echo $maybedir | grep "/$" `
			if [ -z $test ]; then
				fname=$maybedir'/'$TOCHECK
			else
				fname=$maybedir$TOCHECK
			fi

			if [ -f $fname ]; then
				classpath=$classpath:$fname
				echo "Added" $fname "to classpath"
				return
			else
				echo $TOCHECK "is not in" $maybedir
			fi
		fi
		echo $TOCHECK "is not in the classpath"
		echo "Enter the location of" $TOCHECK
		while read fname; do
			if [ -d $fname ]
			then
				maybedir=$fname
				test=` echo $fname | grep "/$" `
				if [ -z $test ]; then
					fname=$maybedir'/'$TOCHECK
				else
					fname=$maybedir$TOCHECK
				fi
				if [ -f $fname ]; then
					classpath=$classpath:$fname
					break
				else
					echo "There is no file" $TOCHECK "in" $maybedir
					echo "trying filename" $fname
				fi
			else
				if [ -f $fname ]; then
					classpath=$classpath:$fname
					break
				else
					echo "The location you entered is not valid, try again"
				fi
			fi
		done
	fi
}

# first check whether the current directory is in the classpath
checkcontainsdot
# will use later in runinter.sh
pureclasspath=$classpath

check="/edu/cmu/sphinx/decoder/AbstractDecoder.class"
checkforfile $check "sphinx4"
if [ $fileinclasspath -eq 0 ]; then
	modifyclasspath "sphinx4.jar"
fi

# check whether sharutils are installed, jsapi.sh requires uudecode
UUDECODE_LOC=`which uudecode`
if [ $UUDECODE_LOC ]; then
	echo "uudecode needed by jsapi.sh is at" $UUDECODE_LOC
else
	echo "jsapi.sh requires uudecode, please install sharutils"
	exit 0
fi

fileinclasspath=0
check="/org/mozilla/javascript/BaseFunction.class"
checkforfile $check "jsapi"
if [ $fileinclasspath -eq 0 ]; then
	modifyclasspath "js.jar"
fi

fileinclasspath=0
check="/WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz/mixture_weights"
checkforfile $check "WSJAudio"
if [ $fileinclasspath -eq 0 ]; then
	modifyclasspath "WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz.jar"
fi

sed "s|myclasspath|$classpath|g" build.in > build.xml

# modify classpath in termplate to include the current directory's bin
pwd=`pwd`
classpath=$classpath:$pwd/bin

sed "s|myclasspath|$classpath|g" runsphinx.in > runsphinx.sh

classpath=$pureclasspath:$pwd/bin
sed "s|myclasspath|$classpath|g" runinter.in > runinter.sh
# cat build.xml
echo "sphinx run script"
cat runsphinx.sh
echo "interpreter run script"
cat runinter.sh

