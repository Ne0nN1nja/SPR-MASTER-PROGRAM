package org.firstinspires.ftc.teamcode;

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


@com.qualcomm.robotcore.eventloop.opmode.Autonomous (name ="Red Auto", group = "")

public class RedAutonomous extends LinearOpMode{

    Robot robot;
    private ElapsedTime runtime = new ElapsedTime();


    static final double     COUNTS_PER_MOTOR_REV    = 1120 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                (WHEEL_DIAMETER_INCHES * Math.PI);
    static final double     DRIVE_SPEED             = 0.8;
    static final double     TURN_SPEED              = 0.5;

    //Camera Stuff
    private static final String TFOD_MODEL_ASSET = "Skystone.tflite";
    private static final String LABEL_FIRST_ELEMENT = "Stone";
    private static final String LABEL_SECOND_ELEMENT = "Skystone";

    private static final String VUFORIA_KEY =
            "AZBogfz/////AAABmYgs9/lPYEyasV9PUNhvydEl01p08nJioFjbQphNqpSsndwEKoX3DUQOxcOIk6rAqnzwlzUL9QqLhvDsMuak4omMgjca0pHxvMS0S8VDZGrOUJ50X0h5hpBHNxJURLP5O9Dzbp9afng1BdYoLGCcPvrVZZ16EWQyH+JWwHrD1AyuzXs7twfTJgCcET0LyJI7bWA6s3F0cNwl2CMeUB4x7MTxNFALg9lhLtJkQEPqSMtbIGKuzUJ7Eem09Ku494o7uYLi0wnTqHoJqrZYSUY+I/CAy07YWN5s3RevgwqthhqNLqISiD7T0U7N6py18mIXcaAqD68SKU8MOtP0XuFnVtLyxjMt8LqftpR2Vie6JdFW";

    private VuforiaLocalizer vuforia;

    private TFObjectDetector tfod;

    @Override
    public void runOpMode(){

        initVuforia();

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        if (tfod != null) {
            tfod.activate();
        }



        robot = new Robot(hardwareMap);

        //int bb8ID = hardwareMap.appContext.getResources().getIdentifier("BB8", "raw", hardwareMap.appContext.getPackageName());

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

        /*telemetry.addData("LAlpha", robot.leftColor.alpha());
        telemetry.addData("RAlpha", robot.rightColor.alpha());
        telemetry.update();*/

        robot.pulleyMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.pulleyMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.pulleyMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        boolean foundation = true;
        //Default start location is foundation side.
        boolean bridge = true;
        //True is against the neutral bridge, false is against the wall

        while (!opModeIsActive()){

            if(gamepad1.a){
                foundation = !foundation;
                sleep(500);
            }
            if(gamepad1.b){
                bridge = !bridge;
                sleep(500);
            }

            //SoundPlayer.getInstance().startPlaying(hardwareMap.appContext, bb8ID);


            telemetry.addData("Starting Location", foundation ? "Foundation" : "Skystone");
            telemetry.addData("Bridge Path", bridge ? "Inside" : "Outside");
            telemetry.update();
        }

        if (tfod != null) {
            tfod.activate();
        }

        waitForStart();

        while (opModeIsActive()){


            if(!foundation) {
                vibeCheck();
                gyroTurn(TURN_SPEED, 90, 3);
                encoderDrive(DRIVE_SPEED, -50, 50, -50, 50, 5);
                robot.lbeltServo.setPosition(0);
                robot.rbeltServo.setPosition(1);
                encoderDrive(DRIVE_SPEED, 6, 6, -6, -6, 2);
                encoderDrive(DRIVE_SPEED, 20, -20, 20, -20, 3);
                sleep(30000);
            }

            if(foundation){
                encoderDrive(DRIVE_SPEED, 23, 23, -23, -23, 3);
                encoderDrive(DRIVE_SPEED, 7, -7, 7, -7, 2);
                gyroTurn(TURN_SPEED, 0, 3);
                encoderDrive(DRIVE_SPEED, 2, 2, -2, -2, 2);
                gyroTurn(TURN_SPEED, 0, 3);
                robot.llatchServo.setPosition(0.5);
                robot.rlatchServo.setPosition(0.0);
                sleep(500);
                encoderDrive(.9, -34, -34, 34, 34, 4);
                robot.llatchServo.setPosition(0.0);
                robot.rlatchServo.setPosition(0.5);
                gyroTurn(TURN_SPEED, 0, 3);
                encoderDrive(DRIVE_SPEED, -40, 40, -40, 40, 4);
                if(bridge){
                    encoderDrive(DRIVE_SPEED, 20, 20, -20, -20, 3);
                }
                /*encoderDrive(DRIVE_SPEED, -25, 25, -25, 25, 3); //In case robot needs to go around foundation and push.
                encoderDrive(DRIVE_SPEED, 36, 36, -36, -36, 5);
                encoderDrive(DRIVE_SPEED, 15, -15, 15, -15, 3);
                encoderDrive(DRIVE_SPEED, -30, -30, 30, 30, 3);
                gyroTurn(TURN_SPEED, 0, 3);
                encoderDrive(DRIVE_SPEED, 12, 12, -12, -12, 3);
                encoderDrive(DRIVE_SPEED, -30, 30, -30, 30, 3);*/
                sleep(30000);
            }
        }

        if (tfod != null) {
            tfod.shutdown();
        }
    }

    //This is the main component for encoder programming.
    public void encoderDrive(double speed, double frontLeftInches, double frontRightInches,
                             double rearLeftInches, double rearRightInches, double timeoutS){

        int newfLeftTarget;
        int newrLeftTarget;
        int newfRightTarget;
        int newrRightTarget;

        if (opModeIsActive()){

            newfLeftTarget = robot.frontLeft.getCurrentPosition() + (int)(frontLeftInches * COUNTS_PER_INCH);
            newfRightTarget = robot.frontRight.getCurrentPosition() + (int)(frontRightInches * COUNTS_PER_INCH);
            newrLeftTarget = robot.rearLeft.getCurrentPosition() + (int)(rearLeftInches * COUNTS_PER_INCH);
            newrRightTarget = robot.rearRight.getCurrentPosition() + (int)(rearRightInches * COUNTS_PER_INCH);
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

    public void hydraulics(double liftSpeed, double height, double timeout){

        int newLiftHeight;

        if (opModeIsActive()) {
            newLiftHeight = robot.liftMotor.getCurrentPosition() + (int) (height * ((145.6) / (.315)));

            robot.liftMotor.setTargetPosition(newLiftHeight);

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

    public void extend(double extendSpeed, double length, double timeOut){

        int newSlideLength;

        if (opModeIsActive()) {

            newSlideLength = robot.pulleyMotor.getCurrentPosition() + (int) (length * COUNTS_PER_INCH);

            robot.pulleyMotor.setTargetPosition(newSlideLength);

            robot.pulleyMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            runtime.reset();

            robot.pulleyMotor.setPower(Math.abs(extendSpeed));

            while (opModeIsActive() &&
                    (runtime.seconds() < timeOut) && (robot.pulleyMotor.isBusy())){

            }
            robot.pulleyMotor.setPower(0);

            robot.pulleyMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            sleep(300);

        }
    }

    /*
    public void colorTest() {
        telemetry.addData("LAlpha", robot.leftColor.alpha());
        telemetry.addData("RAlpha", robot.rightColor.alpha());
        if ((robot.leftColor.alpha() <= 10) && (robot.rightColor.alpha() >= 10)){
            telemetry.addData("Skystone Location:", "Left");
            encoderDrive(DRIVE_SPEED, 6, -6, 6, -6, 3);
        }
        else if ((robot.rightColor.alpha() <= 10) && (robot.leftColor.alpha() >= 10)){
            telemetry.addData("Skystone Location:", "Right");
            encoderDrive(DRIVE_SPEED, -6.5, 6.5, -6.5, 6.5, 3);
        }
        else{
            telemetry.addData("Skystone Location:", "Middle");
        }
        telemetry.update();
    }
     */

    //Steven's second attempt but it actually worked.
    //Instead of relying on the imu to update its angle constantly, this gets the angle robot is at and calculates how much motors need to turn to get to the new angle.

    public void gyroTurn(double turnSpeed, double toDegree, double tolerance) {

        float currentAngle = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
        //targetAngle as in how many degrees the robot needs to rotate to get to the desired angle
        double targetAngle = Math.abs(currentAngle - toDegree);

        boolean goalReached = false;

        if ((currentAngle <= toDegree + tolerance) && (currentAngle >= toDegree - tolerance)) {
            goalReached = true;
        }

        if(!goalReached) {


            if ((currentAngle < toDegree)) {

                encoderDrive(turnSpeed, ((targetAngle * Math.PI) / 180) * 8.5, ((targetAngle * Math.PI) / 180) * -8.5, ((targetAngle * Math.PI) / 180) * -8.5, ((targetAngle * Math.PI) / 180) * 8.5, 5);
                sleep(1000);

            } else if ((currentAngle > toDegree)) {

                encoderDrive(turnSpeed, ((targetAngle * Math.PI) / 180) * -8.5, ((targetAngle * Math.PI) / 180) * 8.5, ((targetAngle * Math.PI) / 180) * 8.5, ((targetAngle * Math.PI) / 180) * -8.5, 5);
                sleep(1000);
            }

        }
    }

    //Dawson's Old imu attempt
    /*public void gyroTurn(double turnSpeed, double degree, double tolerance, double side){
        angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        boolean goalReached = false;

        if ((angles.firstAngle <= degree + tolerance) && (angles.firstAngle >= degree - tolerance)) {
            goalReached = true;
        }
        if (goalReached) {
            robot.frontLeft.setPower(0);
            robot.frontRight.setPower(0);
            robot.rearLeft.setPower(0);
            robot.rearRight.setPower(0);
        }
        if (!goalReached) {
            if (degree < angles.firstAngle) {
                if (side == -1 || side == 0) {
                    robot.frontLeft.setPower(-turnSpeed);
                    robot.frontRight.setPower(turnSpeed);
                    robot.rearLeft.setPower(turnSpeed);
                    robot.rearRight.setPower(-turnSpeed);
                }

                if (side == 1 || side == 0) {
                    robot.frontLeft.setPower(turnSpeed);
                    robot.frontRight.setPower(-turnSpeed);
                    robot.rearLeft.setPower(-turnSpeed);
                    robot.rearRight.setPower(turnSpeed);
                }
            }
            else{
                if (side == -1 || side == 0) {
                    robot.frontLeft.setPower(turnSpeed);
                    robot.frontRight.setPower(-turnSpeed);
                    robot.rearLeft.setPower(-turnSpeed);
                    robot.rearRight.setPower(turnSpeed);
                }

                if (side == 1 || side == 0) {
                    robot.frontLeft.setPower(-turnSpeed);
                    robot.frontRight.setPower(turnSpeed);
                    robot.rearLeft.setPower(turnSpeed);
                    robot.rearRight.setPower(-turnSpeed);
                }
            }
        }*/
    //Steven's Attempt
        /*public void gyroTurn(double turnSpeed, double toDegree, double tolerance){

            angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);


            boolean goalReached = false;

            if ((angles.firstAngle <= toDegree + tolerance) && (angles.firstAngle >= toDegree - tolerance)) {
                goalReached = true;
            }

            if ((angles.firstAngle < toDegree)) {
                robot.frontLeft.setPower(turnSpeed);
                robot.frontRight.setPower(-turnSpeed);
                robot.rearLeft.setPower(-turnSpeed);
                robot.rearRight.setPower(turnSpeed);
            } else if ((angles.firstAngle > toDegree)) {
                robot.frontLeft.setPower(-turnSpeed);
                robot.frontRight.setPower(turnSpeed);
                robot.rearLeft.setPower(turnSpeed);
                robot.rearRight.setPower(-turnSpeed);
            }

            while (!goalReached) {
                telemetry.addData("angle", angles.firstAngle);
                telemetry.update();
                if ((robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle <= toDegree + tolerance) && (robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle >= toDegree - tolerance)) {
                    goalReached = true;
                }
                sleep(1000);
            }

            robot.frontLeft.setPower(0);
            robot.frontRight.setPower(0);
            robot.rearLeft.setPower(0);
            robot.rearRight.setPower(0);

    }*/
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

    private void vibeCheck() {
        if (tfod != null) {
            // getUpdatedRecognitions() will return null if no new information is available since
            // the last time that call was made.
            List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
            if (updatedRecognitions != null) {
                telemetry.addData("# Object Detected", updatedRecognitions.size());
                // step through the list of recognitions and display boundary info.
                for (Recognition recognition : updatedRecognitions) {
                    if ((recognition.getLabel().equals(LABEL_SECOND_ELEMENT)) && (recognition.getLeft() < 300)){
                        telemetry.addData("Skystone Position", "Left");
                        telemetry.update();
                        encoderDrive(DRIVE_SPEED, 10, -10, 10, -10, 3);
                        robot.lbeltServo.setPosition(1);
                        robot.rbeltServo.setPosition(0);
                        gyroTurn(TURN_SPEED, 90, 3);
                        robot.lbeltServo.setPosition(0);
                        robot.rbeltServo.setPosition(1);
                        encoderDrive(DRIVE_SPEED, 4, -4, 4, -4, 2);
                        gyroTurn(TURN_SPEED, 90, 3);
                        encoderDrive(DRIVE_SPEED, -17, -17, 17, 17, 4);
                        robot.lbeltServo.setPosition(1);
                        robot.rbeltServo.setPosition(0);
                        sleep(1000);
                        encoderDrive(DRIVE_SPEED, 10, 10, -10, -10, 3);
                        encoderDrive(DRIVE_SPEED, -4, 4, -4, 4, 2);
                    }
                    else if((recognition.getLabel().equals(LABEL_SECOND_ELEMENT)) && (recognition.getRight() > 550)){
                        telemetry.addData("Skystone Position", "Right");
                        telemetry.update();
                        encoderDrive(DRIVE_SPEED, 10, -10, 10, -10, 3);
                        robot.lbeltServo.setPosition(1);
                        robot.rbeltServo.setPosition(0);
                        gyroTurn(TURN_SPEED, 90, 3);
                        robot.lbeltServo.setPosition(0);
                        robot.rbeltServo.setPosition(1);
                        encoderDrive(DRIVE_SPEED, -4, 4, -4, 4, 2);
                        gyroTurn(TURN_SPEED, 90, 3);
                        encoderDrive(DRIVE_SPEED, -17, -17, 17, 17, 4);
                        robot.lbeltServo.setPosition(1);
                        robot.rbeltServo.setPosition(0);
                        sleep(1000);
                        encoderDrive(DRIVE_SPEED, 10, 10, -10, -10, 3);
                        encoderDrive(DRIVE_SPEED, 4, -4, 4, -4, 2);
                    }
                    else {
                        telemetry.addData("Skystone Position", "Middle");
                        telemetry.update();
                        encoderDrive(DRIVE_SPEED, 10, -10, 10, -10, 3);
                        robot.lbeltServo.setPosition(1);
                        robot.rbeltServo.setPosition(0);
                        gyroTurn(TURN_SPEED, 90, 3);
                        robot.lbeltServo.setPosition(0);
                        robot.rbeltServo.setPosition(1);
                        encoderDrive(DRIVE_SPEED, -2, 2, -2, 2, 2);
                        gyroTurn(TURN_SPEED, 90, 3);
                        encoderDrive(DRIVE_SPEED, -17, -17, 17, 17, 4);
                        robot.lbeltServo.setPosition(1);
                        robot.rbeltServo.setPosition(0);
                        sleep(1000);
                        encoderDrive(DRIVE_SPEED, 10, 10, -10, -10, 3);
                        encoderDrive(DRIVE_SPEED, 2, -2, 2, 2, 2);
                    }

                }
            }
        }
    }
}

