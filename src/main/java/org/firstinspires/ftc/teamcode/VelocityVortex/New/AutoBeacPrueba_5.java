package org.firstinspires.ftc.teamcode.VelocityVortex.New;

import android.graphics.Color;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
/*
1cm = 1022  6cm = 60    11cm = 21
2cm = 440   7cm = 45    12cm = 17
3cm = 225   8cm = 35    13cm = 15
4cm = 130   9cm = 30    14cm = 13
5cm = 85    10cm =24    15cm = 12
 */

/**
 * Created by Jorge on 08/12/2016.
 */
//@Autonomous(name = "AutoBeacPrueba_5")
public class AutoBeacPrueba_5 extends LinearOpMode {
    private DcMotor motorLeftF;
    private DcMotor motorLeftB;
    private DcMotor motorRightF;
    private DcMotor motorRightB;
    private DcMotor LauncherR;
    private DcMotor LauncherL;
    private DcMotor banda;
    private DcMotor aspas;
    Servo ServoR;
    Servo ServoL;
    private ColorSensor LineSensor;
    private ColorSensor BeaconSensor;
    private OpticalDistanceSensor WallSensor;
    private GyroSensor GyroSensor;
    public ModernRoboticsI2cGyro GyroS;

    String white = "white";
    String blue = "blue";
    String red = "red";
    String black = "black";
    String sColor = "";
    String sColorB = "";
    String Multi;

    static double odsReadngRaw;
    static double odsReadingLinear;
    static int odsEstimatedDistance;
    static double m = 56;
    static double b = -0.356;
    double currentHeading;

    @Override
    public void runOpMode() throws InterruptedException {
        motorLeftB = hardwareMap.dcMotor.get("motorLeftB");
        motorLeftF = hardwareMap.dcMotor.get("motorLeftF");
        motorRightB = hardwareMap.dcMotor.get("motorRightB");
        motorRightF = hardwareMap.dcMotor.get("motorRightF");
        LauncherL = hardwareMap.dcMotor.get("LauncherL");
        LauncherR = hardwareMap.dcMotor.get("LauncherR");
        ServoL = hardwareMap.servo.get("ServoL");
        ServoR = hardwareMap.servo.get("ServoR");
        banda = hardwareMap.dcMotor.get("banda");
        aspas = hardwareMap.dcMotor.get("aspas");
        ServoL = hardwareMap.servo.get("ServoL");
        ServoR = hardwareMap.servo.get("ServoR");
        BeaconSensor = hardwareMap.colorSensor.get("BeaconSensor");
        BeaconSensor.setI2cAddress(I2cAddr.create7bit(0x26));//4c
        LineSensor = hardwareMap.colorSensor.get("LineSensor");
        LineSensor.setI2cAddress(I2cAddr.create7bit(0x1e));//3c
        WallSensor = hardwareMap.opticalDistanceSensor.get("WallSensor");
        GyroSensor = hardwareMap.gyroSensor.get("GyroSensor");
        GyroS = (ModernRoboticsI2cGyro)GyroSensor;
        motorLeftB.setDirection(DcMotor.Direction.REVERSE);
        motorLeftF.setDirection(DcMotor.Direction.REVERSE);
        LauncherL.setDirection(DcMotor.Direction.REVERSE);
        LineSensor.enableLed(false);
        BeaconSensor.enableLed(false);

        //BEFORE WE PRESS START GYRO SENSOR NEEDS TO BE CALIBRATED
        telemetry.addData(">", "Gyro Calibrating. Do Not move!");
        telemetry.update();
        GyroSensor.calibrate();

        //WHILE START ISNT PRESSED GIVE SOME TIME TO CALIBRATE
        while (!isStopRequested() && GyroSensor.isCalibrating())  {
            sleep(500);
            idle();
        }

        //SEND CALIBRATION BY TELEMETRY
        telemetry.addData(">", "Gyro Calibrated.  Press Start.");
        telemetry.update();
        sleep(200);
        while (!opModeIsActive()){
            telemetry.addData("Grados: ", GyroS.getIntegratedZValue());
            telemetry.update();
        }

        waitForStart();
        Todo(); //LINE UP WITH WHITE LINE, STOP WHEN IT DETECTS THE LINE, GET LINE UP WHITH THE BEACON
        AvanzarHastaDistanciaX(); //GO FORWARD UNTIL IT DETECTS CERTAIN DISTANCE
        PicarBeacon(); //PRESS 1st BEACON
        BajarServos(); //PUT SERVOS DOWN
        Ir2Beacon(); //GO TO THE NEXT BEACON
        AvanzarHastaDistanciaX2(); //GO FORWARD UNTIL IT DETECTS CERTAIN DISTANCE
        PicarBeacon2(); //PRESS 2nd BEACON
        LanzarParticles(); //SHOOT PARTICLES

    }

    //HERRAMIENTAS - TOOLS
    private void Launchers(double power) {
        //WE ACTIVATE BOTH LAUNCHERS
        LauncherL.setPower(power);
        LauncherR.setPower(power);
    }

    private void SubirPelotas(double power) {
        /*WE TAKE THE PARTICLES UP WITH OUR RUBBER BAND AND MAKE THE BLADES ROTATE TO MAKE SURE THE
        PARTICLE GET INSIDE THE ELEVATOR*/
        banda.setPower(-power);
        aspas.setPower(-power);
    }

    //VUELTAS - TURNS
    public void GirarDerecha(double power) {
        //WE MAKE THE ROBOT TURN RIGHT MAKING BOTH LEFT MOTORS GOING BACKWARDS
        motorRightB.setPower(-power);
        motorRightF.setPower(-power);
        motorLeftB.setPower(power);
        motorLeftF.setPower(power);
    }

    public void GirarIzquierda(double power) {
        //WE MAKE THE ROBOT TURN LEFT MAKING BOTH RIGHT MOTORS GOING BACKWARDS
        motorRightB.setPower(power);
        motorRightF.setPower(power);
        motorLeftB.setPower(-power);
        motorLeftF.setPower(-power);
    }

    public void GirarIzquierda1(double power){
        motorRightB.setPower(power);
        motorRightF.setPower(power);
        motorLeftB.setPower(0);
        motorLeftF.setPower(0);
    }

    public void GirarDerecha1(double power) {
        motorRightB.setPower(0);
        motorRightF.setPower(0);
        motorLeftB.setPower(power);
        motorLeftF.setPower(power);
    }


    //MANEJO - DRIVING
    public void Avanzar(double power) {
        //WE MAKE THE ROBOT TO GO FORWARD GIVING ALL MOTORS THE SAME NEGATIVE POWER
        motorRightB.setPower(-power);
        motorRightF.setPower(-power);
        motorLeftB.setPower(-power);
        motorLeftF.setPower(-power);
    }

    public void Reversa(double power) {
        //WE MAKE THE ROBOT TO GO FORWARD GIVING ALL MOTORS THE SAME POWER
        motorRightB.setPower(power);
        motorRightF.setPower(power);
        motorLeftB.setPower(power);
        motorLeftF.setPower(power);
    }

    //CANCELACIONES - TO STOP
    public void NoDisparar() {
        // WE STOP PARTICLES FROM BEING SHOOT
        Launchers(0);
        SubirPelotas(0);
    }

    public void Frenar() {
        //WE STOP THE ROBOT WITH GIVING 0 POWER TO THIS METHODS
        Avanzar(0);
        Reversa(0);
    }

    //REVISAR COLORES - DETECT COLORS
    public void RevisarColorBeacon() {
        float hsvValues[] = {0, 0, 0};

        //WE USE THE COLOR SENSOR TO DETECT THE THE BEACON COLORS
        Color.RGBToHSV(BeaconSensor.red() * 8, BeaconSensor.green() * 8, BeaconSensor.blue() * 8, hsvValues);
        if (BeaconSensor.blue() > BeaconSensor.red() && BeaconSensor.blue() > BeaconSensor.green()) {
            telemetry.addData("Color", blue);
            telemetry.update();
            sColorB = blue;
        } else {
            telemetry.addData("Color", red);
            telemetry.update();
            sColorB = red;
        }
    }

    //CHECK IF IT DETECTS THE WHITE LINE
    public void RevisarColorLinea() {
        float hsvValues[] = {0, 0, 0};

        Color.RGBToHSV(LineSensor.red() * 8, LineSensor.green() * 8, LineSensor.blue() * 8, hsvValues);
        if (LineSensor.blue() > 0 && LineSensor.red() > 0 && LineSensor.green() > 0) {
            telemetry.addData("Color", white);
            telemetry.update();
            sColor = white;
        } else {
            telemetry.addData("Color", black);
            telemetry.update();
            sColor = black;
        }
    }

    //SEND GYRO SENSOR HEADING WITH TELEMETRY
    public void RevisarVuelta() {
        currentHeading = GyroSensor.getHeading();
        telemetry.addData("Heading", currentHeading);
        telemetry.update();
    }

    public void Todo() {

        // TURN RIGHT TO LINE UP WITH THE WHITE LINE
        //DAR VUELTA DE INICIO  >335
       /* while (GyroSensor.getHeading() > 230) {  //PILA EN 13v ( ), EN -13v (25)
            GirarDerecha1(.25);
        }
        Frenar();
*/
        //GO BACKWARDS AND STOPS WHEN IT DETECTS THE WHITE LINE
        //Avanzar hasta Tape
        //  GirarAnguloX1(42);

        RevisarColorLinea();
        LineSensor.enableLed(true);
        while (sColor.equals(black)) {
            Reversa(.2);
            RevisarColorLinea();
        }
        Frenar();

        sleep(500);

        //LINE UP WITH THE CLOSEST BEACON
        //DAR VUELTA PARA BEACON 1 >290
        GirarAnguloX();

        //SEND IN WICH STAGE OF THE AUTONOMOUS WE ARE BY TELEMETRY
        Multi = "Start";
        telemetry.addLine(Multi);
        telemetry.update();
    }

    //CHECK DISTANCE TO MAKE THE ROBOT STOP BEFORE IT HIT THE WALL
    public void AvanzarHastaDistanciaX() {
        odsReadngRaw = WallSensor.getRawLightDetected();
        odsReadingLinear = Math.pow(odsReadngRaw, -0.5);
        odsEstimatedDistance = (int) ((m * odsReadingLinear) + b);

        while (Multi.equals("Start") && odsEstimatedDistance > 145) {
            odsReadngRaw = WallSensor.getRawLightDetected();
            odsReadingLinear = Math.pow(odsReadngRaw, -0.5);
            odsEstimatedDistance = (int) ((m * odsReadingLinear) + b);
            Avanzar(-.2);
        } //AL LLEGAR A LA POSICION LOS MOTORES SE FRENAN E INICIA UNA NUEVA ETAPA
        motorLeftB.setPower(0);
        motorLeftF.setPower(0);
        motorRightB.setPower(0);
        motorRightF.setPower(0);
        Multi = "Start2";
        telemetry.addLine(Multi);
        telemetry.update();
        sleep(1000);
        //INICIO DE NUEVA ETAPA PARA PARA PICAR BEACON
    }

    //PRESS BEACON BUTTON
    public void PicarBeacon() {
        while (Multi.equals("Start2")) {
            BeaconSensor.enableLed(false);
            RevisarColorBeacon();
            if (sColorB.equals(red)) {
                Avanzar(.1);
                sleep(600);
                Frenar();
                ServoR.setPosition(.5);
                ServoL.setPosition(0);
                Reversa(.1);
                sleep(800);
                Frenar();
                Multi = "Start3";
                telemetry.addLine(Multi);
                telemetry.update();
            } else {
                Avanzar(.1);
                sleep(600);
                Frenar();
                ServoR.setPosition(1);
                ServoL.setPosition(.6);
                Reversa(.1);
                sleep(800);
                Frenar();
                Multi = "Start3";
                telemetry.addLine(Multi);
                telemetry.update();
            }
        }
    }

    //PUT SERVOS DOWN
    public void BajarServos (){
        while (Multi.equals("Start3")){
            ServoR.setPosition(1);
            ServoL.setPosition(0);

            Multi = "Start4";
            telemetry.addLine(Multi);
            telemetry.update();
        }
    }

    //GO TO THE 2nd BEACON
    public void Ir2Beacon() {
        while (Multi.equals("Start4")) {

            //UTILIZAR GYRO SENSOR PARA DAR VUELTA 2
            sleep(400);

            //RETROCEDER
            Avanzar(.15);
            sleep(400);
            Frenar();

            sleep(500);

            GirarAnguloX1();

            Reversa(.2);
            sleep(200);
            Frenar();

            //AVANZAR HASTA TAPE
            RevisarColorLinea();
            LineSensor.enableLed(true);
            while (sColor.equals(black)) {
                Reversa(.2);
                RevisarColorLinea();
            }
            Frenar();

            sleep(500);

            //DAR VUELTA 3 >290
            GirarAnguloX();

            Multi = "Start5";
            telemetry.addLine(Multi);
            telemetry.update();
        }
    }

    //CHECK DISTANCE TO MAKE THE ROBOT STOP BEFORE IT HIT THE WALL
    public void AvanzarHastaDistanciaX2() {
        odsReadngRaw = WallSensor.getRawLightDetected();
        odsReadingLinear = Math.pow(odsReadngRaw, -0.5);
        odsEstimatedDistance = (int) ((m * odsReadingLinear) + b);

        while (Multi.equals("Start5") && odsEstimatedDistance > 145) {
            odsReadngRaw = WallSensor.getRawLightDetected();
            odsReadingLinear = Math.pow(odsReadngRaw, -0.5);
            odsEstimatedDistance = (int) ((m * odsReadingLinear) + b);
            Avanzar(-.2);
        } //AL LLEGAR A LA POSICION LOS MOTORES SE FRENAN E INICIA UNA NUEVA ETAPA
        motorLeftB.setPower(0);
        motorLeftF.setPower(0);
        motorRightB.setPower(0);
        motorRightF.setPower(0);
        Multi = "Start6";
        telemetry.addLine(Multi);
        telemetry.update();
        sleep(1000);
        //INICIO DE NUEVA ETAPA PARA PARA PICAR BEACON
    }

    //PRESS 2nd BEACON
    public void PicarBeacon2() {
        while (Multi.equals("Start6")) {
            BeaconSensor.enableLed(false);
            RevisarColorBeacon();
            if (sColorB.equals(red)) {
                Avanzar(.1);
                sleep(600);
                Frenar();
                ServoR.setPosition(.5);
                ServoL.setPosition(0);
                Reversa(.1);
                sleep(800);
                Frenar();
                Multi = "Start7";
                telemetry.addLine(Multi);
                telemetry.update();
            }
            else {
                Avanzar(.1);
                sleep(600);
                Frenar();
                ServoR.setPosition(1);
                ServoL.setPosition(.6);
                Reversa(.1);
                sleep(800);
                Frenar();
                Multi = "Start7";
                telemetry.addLine(Multi);
                telemetry.update();
            }
        }
    }

    //SHOOT PARTICLES
    public void LanzarParticles(){
        while (Multi.equals("Start7")){

            Avanzar(1);
            sleep(400);
            Frenar();

            sleep(50);

            GirarAnguloX2();

            Avanzar(1);
            sleep(650);
            Frenar();

            Launchers(1);
            sleep(600);
            SubirPelotas(1);
            sleep(2200);
            NoDisparar();

            Avanzar(1);
            sleep(1000);
            Frenar();

            Multi = "Start8";
            telemetry.addLine(Multi);
            telemetry.update();
        }
    }

    public void GirarAnguloX (){
        double zTotal;
        zTotal = GyroS.getIntegratedZValue();
        while (93<zTotal && 87>zTotal){
            GirarDerecha(.15);
            zTotal = GyroS.getIntegratedZValue();
            telemetry.addData("Grados: ", zTotal);
            telemetry.update();
        }
        Frenar();
        zTotal = GyroS.getIntegratedZValue();
        telemetry.addData("Grados: ", zTotal);
        telemetry.update();

    }

    public void GirarAnguloX1 (){
        double zTotal;
        zTotal = GyroS.getIntegratedZValue();
        while (8<zTotal && 3>zTotal){
           GirarIzquierda(.15);
            zTotal = GyroS.getIntegratedZValue();
            telemetry.addData("Grados: ", zTotal);
            telemetry.update();
        }
        Frenar();
        zTotal = GyroS.getIntegratedZValue();
        telemetry.addData("Grados: ", zTotal);
        telemetry.update();
    }
    public void GirarAnguloX2 (){
        double zTotal;
        zTotal = GyroS.getIntegratedZValue();
        while (40<zTotal && 35>zTotal){
            GirarIzquierda(.15);
            zTotal = GyroS.getIntegratedZValue();
            telemetry.addData("Grados: ", zTotal);
            telemetry.update();
        }
        Frenar();
        zTotal = GyroS.getIntegratedZValue();
        telemetry.addData("Grados: ", zTotal);
        telemetry.update();
    }
}
