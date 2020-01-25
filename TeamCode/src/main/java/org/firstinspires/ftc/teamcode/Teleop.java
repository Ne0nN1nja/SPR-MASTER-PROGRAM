package org.firstinspires.ftc.teamcode;


import android.content.Context;

import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp (name = "Teleop")
public class Teleop extends LinearOpMode {

    Robot robot;

    float joystickDeadzone = 0.05f;

    double flMotorPower;
    double frMotorPower;
    double brMotorPower;
    double blMotorPower;
    double pitchServoPos;

    boolean catIn = true;
    boolean RedTeam;
    boolean Selected;

    int soundID;
    boolean soundPlaying = false;

    @Override
    public void runOpMode(){

        robot = new Robot(hardwareMap);
        Context myApp = hardwareMap.appContext;

        robot.llatchServo.setPosition(0.03);
        robot.rlatchServo.setPosition(0.47);
        robot.lbeltServo.setPosition(0);
        robot.rbeltServo.setPosition(1);

        telemetry.addData("INITIALIZED!","WAITING FOR START");
        telemetry.update();

        robot.pulleyMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.liftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.pulleyMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        robot.pulleyMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        robot.rgbDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.GREEN);

        // create a sound parameter that holds the desired player parameters.
        SoundPlayer.PlaySoundParams params = new SoundPlayer.PlaySoundParams();
        params.loopControl = 0;
        params.waitForNonLoopingSoundsToFinish = true;
            if ((soundID = myApp.getResources().getIdentifier("ss_light_saber", "raw", myApp.getPackageName())) != 0) {
                SoundPlayer.getInstance().startPlaying(myApp, soundID, params, null,
                        new Runnable() {
                            public void run() {
                                soundPlaying = false;
                            }
                        });
            }

        while (!opModeIsActive()){
            telemetry.addData("SET ALLIANCE","USE　GAMEPAD 1 TO SET");
            telemetry.addData("X:","RED");
            telemetry.addData("Y:","BLUE");

            Selected = false;
            if(gamepad1.x){
                RedTeam = true;
                Selected = true;
                soundPlaying = true;
                robot.rgbDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.LIGHT_CHASE_RED);
            }
            else if (gamepad1.y){
                RedTeam = false;
                Selected = true;
                soundPlaying = true;
                robot.rgbDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.LIGHT_CHASE_BLUE);
            }

            if (RedTeam && Selected){
                telemetry.addData("SELECTED TEAM", "RED");
            }
            if (!RedTeam && Selected ){
                telemetry.addData("SELECTED TEAM", "BLUE");
            }
            if (!Selected){
                telemetry.addData("SELECTED TEAM", "NEUTRAL");
            }
            telemetry.update();
        }
        waitForStart();

        while (opModeIsActive()) {

            controls();
            telemetry.addData("Pulley Encoder", robot.pulleyMotor.getCurrentPosition());
            telemetry.addData("Lift Encoder", robot.liftMotor.getCurrentPosition());
            telemetry.addData("Pitch Servo Position", robot.pitchServo.getPosition());
            telemetry.addData("Cat Servo Position", robot.catServo.getPosition());
            telemetry.update();
        }

    }

    public void controls() {

        ElapsedTime runtime = new ElapsedTime();

        //Lift Control
        if(!robot.bottomLift.getState() && gamepad2.left_trigger > 0){
            robot.liftMotor.setPower(0);
        }
        else if (gamepad2.right_trigger <= 0.05) {
            robot.liftMotor.setPower(gamepad2.left_trigger / 2);
        }
        else if (gamepad2.left_trigger <= 0.05) {
            robot.liftMotor.setPower(-gamepad2.right_trigger);
        }

        //Pulley Motor Control
        if(robot.pulleyMotor.getCurrentPosition() < 0 && gamepad2.left_stick_y > 0){
            robot.pulleyMotor.setPower(0);
        }
        else if(robot.pulleyMotor.getCurrentPosition() > 3450 && gamepad2.left_stick_y < 0){
            robot.pulleyMotor.setPower(0);
        }
        else {
            robot.pulleyMotor.setPower(-gamepad2.left_stick_y);
        }

        //Latch Servos
        if (gamepad1.y) {
            robot.llatchServo.setPosition(0);
            robot.rlatchServo.setPosition(0.5);
            Lights();
        } else if (gamepad1.x) {
            robot.llatchServo.setPosition(0.5);
            robot.rlatchServo.setPosition(0);
            robot.rgbDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_RED);
        }

        //Belt Servo Control
        if (gamepad1.left_bumper) {
            robot.lbeltServo.setPosition(1);
            robot.rbeltServo.setPosition(0);
        } else {
            robot.lbeltServo.setPosition(0);
            robot.rbeltServo.setPosition(1);
        }

        //Pitch Control
        robot.pitchServo.setPosition(pitchServoPos);

        if (gamepad2.dpad_up) {
            pitchServoPos = (pitchServoPos + 0.02);
            sleep(10);
        } else if (gamepad2.dpad_down) {
            pitchServoPos = (pitchServoPos - 0.02);
            sleep(10);
        } else if (pitchServoPos < 0.05) {
            pitchServoPos = 0.05;
        } else if (pitchServoPos > 0.9) {
            pitchServoPos = 0.9;
        } else if (gamepad2.left_stick_button) {
            pitchServoPos = 0.41;
        }

        //Claw Control
        if (pitchServoPos < 0.1) {
            robot.clawServo.setPosition(0.25);
        } else if (gamepad2.b) {
            robot.clawServo.setPosition(0.50);
        } else if (gamepad2.right_bumper) {
            robot.clawServo.setPosition(0.4);
        } else {
            robot.clawServo.setPosition(0.7);
        }


        //Drivetrain
        if (gamepad1.right_bumper) {
            double joystick1LeftX = gamepad1.left_stick_y;
            double joystick1LeftY = gamepad1.left_stick_x;
            double joystick1RightX = gamepad1.right_stick_x;

            flMotorPower = -joystick1LeftY + joystick1LeftX - joystick1RightX;
            frMotorPower = joystick1LeftY + joystick1LeftX + joystick1RightX;
            brMotorPower = joystick1LeftY - joystick1LeftX - joystick1RightX;
            blMotorPower = -joystick1LeftY - joystick1LeftX + joystick1RightX;

            robot.frontLeft.setPower(flMotorPower);
            robot.frontRight.setPower(frMotorPower);
            robot.rearRight.setPower(brMotorPower);
            robot.rearLeft.setPower(blMotorPower);
        } else {
            double joystick1LeftX = gamepad1.left_stick_y / 3;
            double joystick1LeftY = gamepad1.left_stick_x / 3;
            double joystick1RightX = gamepad1.right_stick_x / 3;

            flMotorPower = -joystick1LeftY + joystick1LeftX - joystick1RightX;
            frMotorPower = joystick1LeftY + joystick1LeftX + joystick1RightX;
            brMotorPower = joystick1LeftY - joystick1LeftX - joystick1RightX;
            blMotorPower = -joystick1LeftY - joystick1LeftX + joystick1RightX;

            robot.frontLeft.setPower(flMotorPower);
            robot.frontRight.setPower(frMotorPower);
            robot.rearRight.setPower(brMotorPower);
            robot.rearLeft.setPower(blMotorPower);
        }

        //Cat Stone
        double catPos = 0.55;

        if (gamepad2.x) {
            catIn = !catIn;
            sleep (200);
        }
        if (catIn) {
            catPos = 0.55;
            Lights();
        }
        if (!catIn) {
            catPos = 0;
            robot.rgbDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.CP1_2_SINELON);
        }
        robot.catServo.setPosition(catPos);


        //Default Claw Position

        if (gamepad2.y) {
        }

    }
    public void Lights(){
        if (RedTeam && Selected){
            robot.rgbDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.SHOT_RED);
        }
        if (!RedTeam && Selected){
            robot.rgbDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.SHOT_BLUE);
        }
        if ((!RedTeam || RedTeam) && !Selected){
            robot.rgbDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.CP1_SHOT);
        }
    }
}

