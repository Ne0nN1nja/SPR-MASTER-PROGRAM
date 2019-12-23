package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

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

        robot.llatchServo.setPosition(0.5);
        robot.rlatchServo.setPosition(0.5);
        /*sleep(1000);
        robot.clawServo.setPosition(0.07);
        robot.pitchServo.setPosition(0.05);*/
        telemetry.clear();
        telemetry.addData("INITIALIZED!","WAITING FOR START");
        telemetry.update();

        robot.pulleyMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        waitForStart();


        while (opModeIsActive()) {

            controls();
            telemetry.addData("Pulley Encoder", robot.pulleyMotor.getCurrentPosition());
            telemetry.update();
        }

    }

    public void controls() {

        //Lift Control
        if (!robot.bottomLift.getState() && robot.topLift.getState()){
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
        }

            robot.pulleyMotor.setPower(-gamepad2.left_stick_y);

        //Latch Servos
        if (gamepad1.y) {
            robot.llatchServo.setPosition(0.5);
            robot.rlatchServo.setPosition(0.5);
        } else if (gamepad1.x) {
            robot.llatchServo.setPosition(0.03);
            robot.rlatchServo.setPosition(0.03);
        }

        robot.pitchServo.setPosition(pitchServoPos);

        if (gamepad2.dpad_up){
            pitchServoPos = (pitchServoPos + 0.05);
            sleep(10);
        }
        else if (gamepad2.dpad_down){
            pitchServoPos = (pitchServoPos - 0.05);
            sleep(10);
        }
        else if (pitchServoPos < 0.05){
            pitchServoPos = 0.05;
        }
        else if (pitchServoPos > 0.9){
            pitchServoPos = 0.9;
        }

        //Belt Servo Control
        if(gamepad1.dpad_left){
            robot.lbeltServo.setPosition(0);
            robot.rbeltServo.setPosition(0);
        }
        else if(gamepad1.dpad_right){
            robot.lbeltServo.setPosition(1);
            robot.rbeltServo.setPosition(1);
        }

        //Claw Control
        if (pitchServoPos < 0.1){
            robot.clawServo.setPosition(0.07);
        }
        else{
            if (gamepad2.b){
                robot.clawServo.setPosition(0.60);
            }
            else if(gamepad2.right_bumper){
                robot.clawServo.setPosition(0.23);
            }
            else {
                robot.clawServo.setPosition(0.27);
            }
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

        //Linear Slide Braking
        if (gamepad2.left_bumper){
            robot.pulleyMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            robot.pulleyMotor.setPower(0);
        }

    }

}
