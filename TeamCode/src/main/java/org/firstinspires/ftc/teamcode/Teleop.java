package org.firstinspires.ftc.teamcode;


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



    @Override
    public void runOpMode(){

        robot = new Robot(hardwareMap);


        robot.llatchServo.setPosition(0.03);
        robot.rlatchServo.setPosition(0.47);
        robot.lbeltServo.setPosition(0);
        robot.rbeltServo.setPosition(1);
        /*sleep(1000);
        robot.clawServo.setPosition(0.07);
        robot.pitchServo.setPosition(0.05);*/
        telemetry.clear();
        telemetry.addData("INITIALIZED!","WAITING FOR START");
        telemetry.update();

        robot.pulleyMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.liftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.pulleyMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
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
        if (gamepad2.right_trigger <= 0.05) {
            robot.liftMotor.setPower(gamepad2.left_trigger/2);
        }
        if (gamepad2.left_trigger <= 0.05) {
            robot.liftMotor.setPower(-gamepad2.right_trigger);
        }
        /*if (!robot.bottomLift.getState() && robot.topLift.getState()){
            robot.liftMotor.setPower(0.1);
            sleep(100);
            robot.liftMotor.setPower(0.0);
        }else if (!robot.topLift.getState() && robot.bottomLift.getState()){
            robot.liftMotor.setPower(-0.1);
            sleep(100);
            robot.liftMotor.setPower(0.0);
        }
        else if (robot.bottomLift.getState()&& robot.topLift.getState()) {
            if (gamepad2.right_trigger <= 0.05) {
                robot.liftMotor.setPower(-gamepad2.left_trigger/2);
            }
            if (gamepad2.left_trigger <= 0.05) {
                robot.liftMotor.setPower(gamepad2.right_trigger);
            }
        }*/

        //Pulley Motor Control
            robot.pulleyMotor.setPower(-gamepad2.left_stick_y);

        //Latch Servos
        if (gamepad1.y) {
            robot.llatchServo.setPosition(0.03);
            robot.rlatchServo.setPosition(0.47);
        } else if (gamepad1.x) {
            robot.llatchServo.setPosition(0.47);
            robot.rlatchServo.setPosition(0.03);
        }

        //Belt Servo Control
        if(gamepad1.left_bumper){
            robot.lbeltServo.setPosition(1);
            robot.rbeltServo.setPosition(0);
        }
        else{
            robot.lbeltServo.setPosition(0);
            robot.rbeltServo.setPosition(1);
        }

        //Pitch Control
        robot.pitchServo.setPosition(pitchServoPos);

        if (gamepad2.dpad_up){
            pitchServoPos = (pitchServoPos + 0.02);
            sleep(10);
        }
        else if (gamepad2.dpad_down){
            pitchServoPos = (pitchServoPos - 0.02);
            sleep(10);
        }
        else if (pitchServoPos < 0.05){
            pitchServoPos = 0.05;
        }
        else if (pitchServoPos > 0.9){
            pitchServoPos = 0.9;
        }
        else if (gamepad2.left_stick_button){
            pitchServoPos= 0.41;
        }

        //Claw Control
        if (pitchServoPos < 0.1){
            robot.clawServo.setPosition(0.25);
        }
        else if (gamepad2.b) {
            robot.clawServo.setPosition(0.50);
        }
        else if (gamepad2.right_bumper) {
            robot.clawServo.setPosition(0.4);
        }
        else {
            robot.clawServo.setPosition(0.7);
        }


        //Drivetrain
        if (gamepad1.right_bumper){
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
        }else{
            double joystick1LeftX = gamepad1.left_stick_y/3;
            double joystick1LeftY = gamepad1.left_stick_x/3;
            double joystick1RightX = gamepad1.right_stick_x/3;

            flMotorPower = -joystick1LeftY + joystick1LeftX - joystick1RightX;
            frMotorPower = joystick1LeftY + joystick1LeftX + joystick1RightX;
            brMotorPower = joystick1LeftY - joystick1LeftX - joystick1RightX;
            blMotorPower = -joystick1LeftY - joystick1LeftX + joystick1RightX;

            robot.frontLeft.setPower(flMotorPower);
            robot.frontRight.setPower(frMotorPower);
            robot.rearRight.setPower(brMotorPower);
            robot.rearLeft.setPower(blMotorPower);
        }

        //Linear Slide Manual Braking
        if (gamepad2.left_bumper){
            robot.pulleyMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            robot.pulleyMotor.setPower(0);
        }

        //Cat Stone
        if (gamepad2.x){
            robot.catServo.setPosition(0);
        }
        else{
            robot.catServo.setPosition(0.87);
        }


        //Default Claw Position

        if (gamepad2.y){
            liftgrabPos();
        }

    }

    public void liftgrabPos() {

        ElapsedTime runtime = new ElapsedTime();

        robot.liftMotor.setTargetPosition(480);
        robot.liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        pitchServoPos = .41;

        runtime.reset();

        while (robot.liftMotor.isBusy() && runtime.seconds() < 2) {
            robot.liftMotor.setPower(0.75);
        }
        robot.liftMotor.setPower(0);
        robot.liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    }






}

