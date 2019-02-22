# Sekmentation

# Build instructions

This repository contains two algorithms for segmenting an 
ultrasound infrarenal aorta image.

To run the demo compile the project using the gradle wrapper
(no need to install gradle):
```
./gradlew build
```

This command will output the `sekmentation-1.0.jar` file which can
be executed with the command:
```
java -jar sekmentation-1.0.jar
```
# Algorithms

The implementation of the Level Set algorithm can be found [here](https://github.com/TomasVolker/sekmentation/blob/master/src/main/kotlin/numeriko/sekmentation/levelset/SimpleLevelSet.kt)

The implementation of the Fuzzy Region Growing algorithm can be found [here](https://github.com/TomasVolker/sekmentation/blob/master/src/main/kotlin/numeriko/sekmentation/fuzzyregiongrowing/FuzzyConnectedness.kt)
