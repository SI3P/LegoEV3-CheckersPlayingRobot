# CheckersRobot
This project has been realized as subsidiary formative activite (Lego Lab) for the Master Course in Computer Science at the University of Rome "La Sapienza".

The robot consists of four units:

1) Six-wheeled carriage FWD (two-wheel drive). The task of the unit is to move the robot along the edge of the board (X-axis)

2) Carrier. This unit places the arm of robot over chessboard (Y-axis).

3) Scissor lift. This unit lifts and lowers the robot arm.

4) Claw. Simply grabs the pawns. Unlike the other units which use large servo motors, this unit uses a medium motor.

The movements of the human player are captured by a webcam fixed on one of the free sides, the images are analyzed to detect which pieces have been moved and the result is sent to the draughts engine that will give the next move which will send to the EV3 by bluetooth.
The whole project is written in Java. The checkers engine implements the MiniMax algorithm with alpha-beta pruning and the quiescent search. The code that analyzes the images makes use of OpenCV library.

### Screenshot
<img src="screenshot/ev3_checkers_robot.png?raw=true" width="600"/>

### Video
https://www.youtube.com/watch?v=rRNOXEZBs28

### Picture
https://plus.google.com/photos/photo/104626533427419398246/6207104140331136786

### Design
https://plus.google.com/photos/104626533427419398246/album/6260873768281079857

### Building Instructions
https://drive.google.com/open?id=0B9II24hvksAKd0dfSUo4a0lXd2s
