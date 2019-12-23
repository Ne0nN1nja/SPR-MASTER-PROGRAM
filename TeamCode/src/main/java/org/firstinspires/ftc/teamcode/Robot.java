package org.firstinspires.ftc.teamcode;


import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Robot {

    //Drive Motors
    public DcMotor frontRight;
    public DcMotor frontLeft;
    public DcMotor rearRight;
    public DcMotor rearLeft;
    public DcMotor liftMotor;
    public DcMotor pulleyMotor;


//Hi Jake
    //Servos
    public Servo llatchServo;
    public Servo rlatchServo;
    public Servo pitchServo;
    public Servo clawServo;
    public Servo lbeltServo;
    public Servo rbeltServo;
    public Servo catServo;


    //Sensors
    public DigitalChannel topLift;
    public DigitalChannel bottomLift;

    //For REV Gyro
    public BNO055IMU imu;

    //RGB!!!
    public RevBlinkinLedDriver rgbDriver;

    //Webcam

    public Robot(HardwareMap hardwareMap){
//        rgbDriver = hardwareMap.get(RevBlinkinLedDriver.class, "ledDriver");
//        rgbDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.RAINBOW_WITH_GLITTER);


        //Drive Motors
        frontLeft = hardwareMap.dcMotor.get("lfWheel");
        frontRight = hardwareMap.dcMotor.get("rfWheel");
        rearLeft = hardwareMap.dcMotor.get("lrWheel");
        rearRight = hardwareMap.dcMotor.get("rrWheel");
        llatchServo = hardwareMap.servo.get("llatchServo");
        rlatchServo = hardwareMap.servo.get("rlatchServo");
        pulleyMotor = hardwareMap.dcMotor.get("pulleyMotor");
        liftMotor = hardwareMap.dcMotor.get("liftMotor");
        pitchServo = hardwareMap.servo.get("pitchServo");
        clawServo = hardwareMap.servo.get("clawServo");
        lbeltServo = hardwareMap.servo.get("lbeltServo");
        rbeltServo = hardwareMap.servo.get("rbeltServo");
        catServo = hardwareMap.servo.get("catServo");


        topLift = hardwareMap.get(DigitalChannel.class, "topLift");
        bottomLift = hardwareMap.get(DigitalChannel.class, "bottomLift");
        topLift.setMode(DigitalChannel.Mode.INPUT);
        bottomLift.setMode(DigitalChannel.Mode.INPUT);

        frontRight.setDirection(DcMotor.Direction.REVERSE);
        rearLeft.setDirection(DcMotor.Direction.REVERSE);

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);


    }


}