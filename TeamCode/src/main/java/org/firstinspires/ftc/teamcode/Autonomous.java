package org.firstinspires.ftc.teamcode;

import android.content.Context;

import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;


@com.qualcomm.robotcore.eventloop.opmode.Autonomous (name ="Autonomous", group = "")

public class Autonomous extends LinearOpMode{

    Robot robot;
    private ElapsedTime runtime = new ElapsedTime();


    static final double     COUNTS_PER_MOTOR_REV    = 1120 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                (WHEEL_DIAMETER_INCHES * Math.PI);
    static final double     DRIVE_SPEED             = 0.8;
    static final double     TURN_SPEED              = 0.6;

    //Camera Stuff
    private static final String TFOD_MODEL_ASSET = "Skystone.tflite";
    private static final String LABEL_FIRST_ELEMENT = "Stone";
    private static final String LABEL_SECOND_ELEMENT = "Skystone";

    private static final String VUFORIA_KEY =
            "AZBogfz/////AAABmYgs9/lPYEyasV9PUNhvydEl01p08nJioFjbQphNqpSsndwEKoX3DUQOxcOIk6rAqnzwlzUL9QqLhvDsMuak4omMgjca0pHxvMS0S8VDZGrOUJ50X0h5hpBHNxJURLP5O9Dzbp9afng1BdYoLGCcPvrVZZ16EWQyH+JWwHrD1AyuzXs7twfTJgCcET0LyJI7bWA6s3F0cNwl2CMeUB4x7MTxNFALg9lhLtJkQEPqSMtbIGKuzUJ7Eem09Ku494o7uYLi0wnTqHoJqrZYSUY+I/CAy07YWN5s3RevgwqthhqNLqISiD7T0U7N6py18mIXcaAqD68SKU8MOtP0XuFnVtLyxjMt8LqftpR2Vie6JdFW";

    private VuforiaLocalizer vuforia;

    private TFObjectDetector tfod;

    //Stores Skystone Position
    int skystonePos = 0;

    //Team
    boolean teamRed = true;

    //Staging Start Point
    int stage = 0;

    //Sound Variables
    int soundID;
    boolean soundPlaying = false;

    @Override
    public void runOpMode(){

        //Camera Initiation
        initVuforia();

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        if (tfod != null) {
            tfod.activate();
        }

        //Robot Hardware Initialization
        robot = new Robot(hardwareMap);
        Context myApp = hardwareMap.appContext;

        // create a sound parameter that holds the desired player parameters.
        SoundPlayer.PlaySoundParams params = new SoundPlayer.PlaySoundParams();
        params.loopControl = 0;
        params.waitForNonLoopingSoundsToFinish = true;
        if ((soundID = myApp.getResources().getIdentifier("beepboopbeep", "raw", myApp.getPackageName())) != 0) {
            SoundPlayer.getInstance().startPlaying(myApp, soundID, params, null,
                    new Runnable() {
                        public void run() {
                            soundPlaying = false;
                        }
                    });
        }

        //Resetting servos and motors
        robot.llatchServo.setPosition(0.0);
        robot.rlatchServo.setPosition(0.5);

        robot.frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        robot.pulleyMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.pulleyMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.pulleyMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        runtime.reset();
        while(robot.bottomLift.getState() && runtime.seconds() < 4){
            robot.liftMotor.setPower(0.5);
        }

        robot.liftMotor.setPower(0);
        robot.liftMotor.setMode((DcMotor.RunMode.STOP_AND_RESET_ENCODER));
        robot.liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        boolean foundation = true;
        //Default start location is foundation side.
        boolean bridge = true;
        //True is against the neutral bridge (inside), false is against the wall (outside)
        boolean parkOverride = false;
        //Only run if the other team has a vastly superior code and ours will interfere with it.
        boolean crossover = true;
        //Mostly for foundation side. Determines if the robot will attempt to grab a block if on foundation side.

        while (!opModeIsActive()){

            if(gamepad1.right_bumper){
                teamRed = !teamRed;
                sleep(300);
            }
            if(gamepad1.a){
                foundation = !foundation;
                sleep(300);
            }
            if(gamepad1.b){
                bridge = !bridge;
                sleep(300);
            }
            if(gamepad1.x){
                crossover = !crossover;
                sleep(300);
            }
            if(gamepad1.y){
                parkOverride = !parkOverride;
                sleep(300);
            }

            //Changes the LED colors based on team
            if(teamRed){
                robot.rgbDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.LIGHT_CHASE_RED);
            }
            else{
                robot.rgbDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.LIGHT_CHASE_BLUE);
            }

            telemetry.addData("Team(RB)", teamRed ? "Red" : "Blue");
            telemetry.addData("Crossover(X)", crossover ? "Yes" : "No");
            telemetry.addData("Starting Location(A)", foundation ? "Foundation" : "Skystone");
            telemetry.addData("Bridge Passing and Parking(B)", bridge ? "Inside" : "Outside");
            telemetry.addData("Parking Override(Y)", parkOverride ? "Yes" : "No");
            telemetry.update();
        }

        if (tfod != null) {
            tfod.activate();
        }

        waitForStart();

        while (opModeIsActive()){

            telemetry.addData("Stage", stage);
            telemetry.addData("Current Angle", robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle);
            telemetry.addData("Skystone Position", skystonePos);
            telemetry.update();
            if(parkOverride){
                stage = 69;
            }

            if(!foundation){
                switch(stage){
                    case 0: //Identify skystone
                        vibeCheck();
                        sleep(500);
                        stage++;
                        break;
                    case 1: //Get away from wall and rotate toward blocks.
                        Strafe(DRIVE_SPEED, 15, 3, true, false);
                        defaultPos();
                        gyroTurn(TURN_SPEED, 90, 3, false);
                        //This is where things split for a moment. At the end of these branches, they go to the same spot.
                        switch(skystonePos){
                            case 0: //Skystone is in the middle, so it goes forward, grabs, and lines up to deliver.
                                Straight(0.5, 10, 2, true, false);
                                Claw(true, 500);
                                Straight(DRIVE_SPEED, 7, 2, false, false);
                                break;
                            case 1: //Skystone is to the left. Goes left, forward, back, right.
                                Strafe(DRIVE_SPEED, 6, 2, true, false);
                                Straight(0.5, 10, 2, true, false);
                                Claw(true, 500);
                                Straight(DRIVE_SPEED, 7, 2, false, false);
                                Strafe(DRIVE_SPEED, 6, 2, false, false);
                                break;
                            case 2: //Skystone is to the right. Goes right, forward, back, left.
                                Strafe(DRIVE_SPEED, 8, 2, false, false);
                                Straight(0.5, 10, 2, true, false);
                                Claw(true, 500);
                                Straight(DRIVE_SPEED, 7, 2, false, false);
                                Strafe(DRIVE_SPEED, 8, 2, true, false);
                                break;
                        }
                        stage++;
                        break;
                    case 2: //Drive under bridge
                        if(!bridge){
                            Straight(DRIVE_SPEED, 20, 2, false, false);
                        }
                        Strafe(DRIVE_SPEED, 40, 4,  false, true);
                        stage++;
                        break;
                    case 3: //Let go of block and go back for second block.
                        Claw(false, 500);
                        switch(skystonePos){
                            case 0:
                                Strafe(DRIVE_SPEED, 55, 4, true, true);
                                break;
                            case 1:
                                Strafe(DRIVE_SPEED, 55, 4, true, true);
                                break;
                            case 2:
                                Strafe(DRIVE_SPEED, 45 , 4, true, true);
                        }
                        stage++;
                        break;
                    case 4: //Turn and grab the block
                        gyroTurn(TURN_SPEED, 90, 3, false);
                        Straight(0.5, 7, 2, true, false);
                        Claw(true, 500);
                        Straight(DRIVE_SPEED, 7, 2, false, false);
                        stage++;
                        break;
                    case 5: //Deliver and park
                        gyroTurn(TURN_SPEED, 0, 5, true);
                        Straight(DRIVE_SPEED, 60, 4, true, false);
                        Straight(DRIVE_SPEED, 20, 4, false, false);
                        Hydraulics(0.9, 3.5, 2);
                        stage++;
                        break;
                    case 69:
                        Straight(DRIVE_SPEED, 25, 3, true, true);
                        stage++;
                        break;
                }

            }

            if(foundation){
                switch(stage){
                    case 0: //Drive to foundation
                        Straight(DRIVE_SPEED, 23, 3, false, false);
                        Strafe(DRIVE_SPEED, 9, 2, true, true);
                        gyroTurn(TURN_SPEED, 0, 3, false);
                        stage++;
                        break;
                    case 1: //Go and latch onto foundation
                        Straight(DRIVE_SPEED, 3, 1, false, false);
                        gyroTurn(TURN_SPEED, 0, 3, false);
                        Latch(true, 500);
                        stage++;
                        break;
                    case 2: //Pull foundation to site and realign itself
                        Straight(1, 36, 4, true, false);
                        Latch(false, 500);
                        gyroTurn(TURN_SPEED, 0 ,3, false);
                        stage++;
                        break;
                    case 3: //Split between parking or go for a random block
                        if(bridge){
                            Strafe(DRIVE_SPEED, 25, 4, false, true);
                            Straight(DRIVE_SPEED, 20, 2, false, false);
                            if(crossover){
                                gyroTurn(TURN_SPEED, 180, 3, false);
                                stage++;
                            }
                            else {
                                Strafe(DRIVE_SPEED, 20, 4, false, true);
                            }
                        }
                        else{
                            Strafe(DRIVE_SPEED, 40, 4, false, true);
                        }
                        stage++;
                        break;
                    case 69:
                        Strafe(DRIVE_SPEED, 30, 3, false, true);
                        stage++;
                        break;
                }

            }
        }

        if (tfod != null) {
            tfod.shutdown();
        }
    }

    //This is the main component for encoder programming.
    public void encoderDrive(double speed, double frontLeftInches, double frontRightInches,
                             double rearLeftInches, double rearRightInches, double timeoutS) {

        int newfLeftTarget;
        int newrLeftTarget;
        int newfRightTarget;
        int newrRightTarget;

        if (opModeIsActive()) {

            newfLeftTarget = robot.frontLeft.getCurrentPosition() + (int) (frontLeftInches * COUNTS_PER_INCH);
            newfRightTarget = robot.frontRight.getCurrentPosition() + (int) (frontRightInches * COUNTS_PER_INCH);
            newrLeftTarget = robot.rearLeft.getCurrentPosition() + (int) (rearLeftInches * COUNTS_PER_INCH);
            newrRightTarget = robot.rearRight.getCurrentPosition() + (int) (rearRightInches * COUNTS_PER_INCH);
            robot.frontLeft.setTargetPosition(newfLeftTarget);
            robot.frontRight.setTargetPosition((newfRightTarget));
            robot.rearLeft.setTargetPosition(newrLeftTarget);
            robot.rearRight.setTargetPosition(newrRightTarget);

            robot.frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rearLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rearRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            runtime.reset();
            robot.frontLeft.setPower(Math.abs(speed));
            robot.frontRight.setPower(Math.abs(speed));
            robot.rearLeft.setPower(Math.abs(speed));
            robot.rearRight.setPower(Math.abs(speed));

            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (robot.frontLeft.isBusy() && robot.frontRight.isBusy() &&
                            robot.rearLeft.isBusy() && robot.rearRight.isBusy())) {

            }

            robot.frontLeft.setPower(0);
            robot.frontRight.setPower(0);
            robot.rearLeft.setPower(0);
            robot.rearRight.setPower(0);

            robot.frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            sleep(300);
        }
    }
    //Methods for encoder drive because typing out 4 of the same numbers is tedious.
    public void Straight(double speed, double inch, double timeout, boolean forward, boolean blueReverse){
        if (!forward){
            inch = -inch;
        }
        if (!teamRed && blueReverse){ //This effectively converts it to blue code.
            inch = -inch;
        }
        encoderDrive(speed, -inch, -inch, inch, inch, timeout);
    }

    public void Strafe(double speed, double inch, double timeout, boolean left, boolean blueReverse) {
        if (!left) {
            inch = -inch;
        }
        if (!teamRed && blueReverse){ //This effectively converts it to blue code.
            inch = -inch;
        }
        encoderDrive(speed, inch, -inch, inch, -inch, timeout);
    }

    //Servo Opening and Closing Methods

    public void Grab (boolean closed, int sleepms){ //For the "fingers"
        if(!closed){
            robot.lbeltServo.setPosition(0);
            robot.rbeltServo.setPosition(1);
        }
        if(closed){
            robot.lbeltServo.setPosition(1);
            robot.rbeltServo.setPosition(0);
        }
        sleep(sleepms);
    }

    public void Latch (boolean down, int sleepms){
        if(!down){
            robot.llatchServo.setPosition(0);
            robot.rlatchServo.setPosition(0.5);
        }
        if(down){
            robot.llatchServo.setPosition(0.5);
            robot.rlatchServo.setPosition(0);
        }
        sleep(sleepms);
    }

    public void Claw (boolean down, int sleepms){
        if(!down){
            robot.clawServo.setPosition(0.7);
        }
        if(down){
            robot.clawServo.setPosition(0.5);
        }
        sleep(sleepms);
    }

    //Screw Lift
    public void Hydraulics(double liftSpeed, double height, double timeout){

        int newLiftHeight;

        if (opModeIsActive()) {
            newLiftHeight = robot.liftMotor.getCurrentPosition() + (int) (height * ((145.6) / (.315)));

            robot.liftMotor.setTargetPosition(-newLiftHeight);

            robot.liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            runtime.reset();

            robot.liftMotor.setPower(Math.abs(liftSpeed));

            while (opModeIsActive() &&
                    (runtime.seconds() < timeout) && (robot.liftMotor.isBusy())){

            }

            robot.liftMotor.setPower(0);

            robot.liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            sleep(300);

        }
    }

    //Lift Arm
    public void Extend(double extendSpeed, double length, double timeout){

        int newSlideLength;

        if (opModeIsActive()) {

            newSlideLength = robot.pulleyMotor.getCurrentPosition() + (int) (length * (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                    (1.26 * Math.PI));

            robot.pulleyMotor.setTargetPosition(newSlideLength);

            robot.pulleyMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            runtime.reset();

            robot.pulleyMotor.setPower(Math.abs(extendSpeed));

            while (opModeIsActive() &&
                    (runtime.seconds() < timeout) && (robot.pulleyMotor.isBusy())){

            }
            robot.pulleyMotor.setPower(0);

            robot.pulleyMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            sleep(300);

        }
    }

    //Steven's second attempt but it actually worked.
    //Instead of relying on the imu to update its angle constantly, this gets the angle robot is at and calculates how much motors need to turn to get to the new angle.
    //Technically this is an encoder turn using the gyro as an input.
    public void gyroTurn(double turnSpeed, double toDegree, double tolerance, boolean reverseBlue) {

        boolean goalReached = false;
        int spinCount = 0;

        if(!teamRed && reverseBlue){
            toDegree = -toDegree;
            if(toDegree == 0){
                toDegree = 180;
                spinCount = 2;
            }
        }

        while(!goalReached && spinCount < 3){

            float currentAngle = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
            //targetAngle as in how many degrees the robot needs to rotate to get to the desired angle
            double targetAngle = Math.abs(currentAngle - toDegree);

            double inches =  ((targetAngle * Math.PI) / 180) * 8;

            if((currentAngle <= toDegree + tolerance) && (currentAngle >= toDegree - tolerance)){
                goalReached = true;
            }
            else if ((currentAngle < toDegree)) {
                encoderDrive(turnSpeed, inches, -inches, -inches, inches, 3);
                sleep(500);

            }
            else if ((currentAngle > toDegree)) {
                encoderDrive(turnSpeed, -inches, inches, inches, -inches, 3);
                sleep(500);
            }
            spinCount++;
        }
    }

    public void defaultPos(){
        Hydraulics(0.9, 3.5, 2);
        robot.pitchServo.setPosition(0.41);
        robot.clawServo.setPosition(0.7);
        sleep(1000);
        Hydraulics(0.5, 3, 2);
    }

    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam 1");

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

    /**
     * Initialize the TensorFlow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minimumConfidence = 0.8;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT);
    }

    //Skystone Detection based on left and right coordinates of stone.
    private void vibeCheck() {
        if (tfod != null) {
            // getUpdatedRecognitions() will return null if no new information is available since
            // the last time that call was made.
            List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
            if (updatedRecognitions != null) {
                telemetry.addData("# Object Detected", updatedRecognitions.size());
                // step through the list of recognitions and display boundary info.
                for (Recognition recognition : updatedRecognitions) {
                    if ((recognition.getLabel().equals(LABEL_SECOND_ELEMENT)) && (recognition.getLeft() < 250)){
                        //Left Side
                        skystonePos = 1;
                    }
                    else if((recognition.getLabel().equals(LABEL_SECOND_ELEMENT)) && (recognition.getRight() > 500)){
                        skystonePos = 2;

                    }
                    else { //In the middle
                        skystonePos = 0;
                    }

                }
                telemetry.update();
            }
        }
    }
}

