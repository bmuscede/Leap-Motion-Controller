Leap Motion Controller
======================

### Project Details
This is the software for the Leap Motion Controller thesis. This repo contains all the required files to build the Visualizer and main program. This is for Computer Science 4490Z thesis at The University of Western Ontario. The premise of this project is to create a tracking tool that allows for medical residents to be assessed on surgical skill by analyzing hand motions. This comes from [this paper](https://academic.oup.com/ejcts/article/39/3/287/353822/Training-and-assessment-of-technical-skills-and).

### Project Contents
This repo contains two directories:
- The Leap Motion Tracker (*LeapMotionTracker*)
    - This contains the files for the Java program.
    - This program is the main controller for the project. It tracks hand motions, saves them to the database and outputs metrics based on a user's performance.
- The Leap Motion Visualizer (*LeapMotionVisualizer*)
    - This contains the files for the C# and Unity program.
    - This program is the visualizer for the project. While it is not needed for program functionality, it visualizes a user's hand motions in real time and shows them what the Leap Motion Controller is seeing.

### Installation
Currently, there is no good way to build the Leap Motion Controller project. To build, the Java and Unity programs need to be built separately.

To build the Java program:
```sh
$ cd LeapMotionTracker
$ javac -d bin -sourcepath src -cp lib/LeapJava.jar;lib/sqlite-j dbc-3.8.7.jar src/ProgramController.java
$ java -cp bin;lib/LeapJava.jar;lib/sqlite-jdbc-3.8.7.jar ProgramController
```

To build the Unity visualizer:

1. Run Unity 4.0
2. In the *Project Wizard Dialog* select *Open Other...*
3. Navigate to the *LeapMotionVisualizer* directory.
4. Click *File>Build...*
5. Select desired build options.
6. Save *Visualizer.exe* in *LeapMotionTracker/Visualizer*
7. Move *LeapCSharp.dll* and *Leap.dll* into the *Visaulizer* directory.

In the future, a script will be produced that will automate the building of the Leap Motion Visualizer and the Leap Motion Tracker.
