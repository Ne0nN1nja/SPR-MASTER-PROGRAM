package org.firstinspires.ftc.teamcode;


import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.Locale;

@TeleOp (name = "Device Tester")
public class Test extends LinearOpMode {

    Robot robot;

    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";
    private static final String VUFORIA_KEY = "AZBogfz/////AAABmYgs9/lPYEyasV9PUNhvydEl01p08nJioFjbQphNqpSsndwEKoX3DUQOxcOIk6rAqnzwlzUL9QqLhvDsMuak4omMgjca0pHxvMS0S8VDZGrOUJ50X0h5hpBHNxJURLP5O9Dzbp9afng1BdYoLGCcPvrVZZ16EWQyH+JWwHrD1AyuzXs7twfTJgCcET0LyJI7bWA6s3F0cNwl2CMeUB4x7MTxNFALg9lhLtJkQEPqSMtbIGKuzUJ7Eem09Ku494o7uYLi0wnTqHoJqrZYSUY+I/CAy07YWN5s3RevgwqthhqNLqISiD7T0U7N6py18mIXcaAqD68SKU8MOtP0XuFnVtLyxjMt8LqftpR2Vie6JdFW";
    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;
    //Color Stuff
    /*float hsvValues[] = {0F, 0F, 0F};
    final float values[] = hsvValues;
    final double SCALE_FACTOR = 255;*/

    //int relativeLayoutId = hardwareMap.appContext.getResources().getIdentifier("RelativeLayout", "id", hardwareMap.appContext.getPackageName());
    //final View relativeLayout = ((Activity) hardwareMap.appContext).findViewById(relativeLayoutId);


    @Override
    public void runOpMode(){

        robot = new Robot(hardwareMap);
        telemetry.addData("Make sure all the encoder cables are plugged in!", "");
        telemetry.update();

        initVuforia();

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        /** Wait for the game to begin */
        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();



        waitForStart();

        while (opModeIsActive()){

            if(gamepad1.x){
                encoderTest();
            }

            if(gamepad1.y){
                //colorTest();
            }

            if(gamepad1.a){
                magSwitch();
            }

            telemetry.update();

            }
        }

    public void encoderTest(){
        if(gamepad1.a){
            robot.frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            robot.frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            robot.rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            robot.rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            robot.pulleyMotor.setMode((DcMotor.RunMode.STOP_AND_RESET_ENCODER));

            robot.frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.pulleyMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        telemetry.addData("frontLeft motor position:", robot.frontLeft.getCurrentPosition());
        telemetry.addData("frontRight motor position:", robot.frontRight.getCurrentPosition());
        telemetry.addData("rearLeft motor position:", robot.rearLeft.getCurrentPosition());
        telemetry.addData("rearRight motor position:", robot.rearRight.getCurrentPosition());
        telemetry.addData("pulleyMotor position:", robot.pulleyMotor.getCurrentPosition());
    }
/*
    public void colorTest(){
        Color.RGBToHSV((int) (robot.leftColor.red() * SCALE_FACTOR),
                (int) (robot.leftColor.green() * SCALE_FACTOR),
                (int) (robot.leftColor.blue() * SCALE_FACTOR),
                hsvValues);
        telemetry.addData("Distance (cm)",
                String.format(Locale.US, "%.02f", sensorDistance.getDistance(DistanceUnit.CM)));
        telemetry.addData("Alpha", robot.leftColor.alpha());
        telemetry.addData("Red  ", robot.leftColor.red());
        telemetry.addData("Green", robot.leftColor.green());
        telemetry.addData("Blue ", robot.leftColor.blue());

        telemetry.addData("Alpha", robot.rightColor.alpha());
        telemetry.addData("Red  ", robot.rightColor.red());
        telemetry.addData("Green", robot.rightColor.green());
        telemetry.addData("Blue ", robot.rightColor.blue());
    }
    */
    public void magSwitch(){
        telemetry.addData("Top Lift", robot.topLift.getState());
        telemetry.addData("Bottom Lift", robot.bottomLift.getState());
    }

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

    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }
}
