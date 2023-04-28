// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Encoder;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import java.lang.Math;


/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private static final String kFastAuto = "FasterAuto";
  private static final String kCaseToCase = "CaseToCase";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  public Joystick joystick0 = new Joystick(0);
  public TalonFX motor1 = new TalonFX(1);

  public double error = 0;

  public boolean zeroToggle = false;

  public double wanted = 0;

  public double encoderPos = 0;
  public double encoderVel = 0;

  public double magicNumber = 0.000025;
  public double boundsOfDegree = 10;

  public double previousEncoderPos = 0;
  // Wheel diamater, in inches.
  public double wheelDiameter = 3;

  // Circumference, in inches.
  public double wheelCircumference = Math.PI * wheelDiameter;

  public int auto = 0;
  public double drivingDistance = 0;
  
/**
 * JL here! driveInDistance will use an encoder to drive a set distance,
 * calculated using the built in encoder in a while loop until the distance is met.
 * The 
 * @param distance
 * The distance the robot will be travelling, in feet.
 * 
 * @param power
 * The strength of the robot's driving of a range from -1.0 to 1.0.
 */
  public void driveInDistance(double distance, double power){
    previousEncoderPos = encoderPos;
    while (((encoderPos - previousEncoderPos) * 360 * wheelCircumference) / 12 < distance){
      motor1.set(ControlMode.PercentOutput, power);
    }

  }

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("Return to Position", kCustomAuto);
    m_chooser.addOption("Return to positionFAST", kFastAuto);
    m_chooser.addOption("Case To Case", kCaseToCase);
    SmartDashboard.putData("Auto choices", m_chooser);
    
    motor1.setNeutralMode(NeutralMode.Brake);

    

    auto = 0;
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override 
  public void robotPeriodic() {

    error = wanted - encoderPos;
    encoderPos = (motor1.getSelectedSensorPosition() / 2048) * 360;
    encoderVel = (motor1.getSelectedSensorVelocity() / 2048) * 360 * 10;

    SmartDashboard.putNumber("Encoder Raw Value", motor1.getSelectedSensorPosition());
    SmartDashboard.putNumber("Encoder Rotations", motor1.getSelectedSensorPosition() / 2048);
    SmartDashboard.putNumber("Encoder Degrees", encoderPos);

    SmartDashboard.putNumber("Encoder Raw Velocity", motor1.getSelectedSensorVelocity());
    SmartDashboard.putNumber("Encoder RPS", (motor1.getSelectedSensorVelocity() / 2048) * 10);
    SmartDashboard.putNumber("Encoder Degrees/Sec", encoderVel);

    SmartDashboard.putNumber("degreeBounds", boundsOfDegree);
    //boundsOfDegree = SmartDashboard.getNumber("degreeBounds", 10);

    SmartDashboard.putNumber("Current error", error);
    SmartDashboard.putNumber("Current Proposed Power", error * magicNumber);
    SmartDashboard.putNumber("Magic Number", magicNumber);
    //magicNumber = SmartDashboard.getNumber("Magic Number", 0);

    SmartDashboard.putNumber("Distance (in)", encoderPos * 360 * wheelCircumference);

    SmartDashboard.putBoolean("Zero Toggle", zeroToggle);
    SmartDashboard.putNumber("driveDistance", drivingDistance);

    if(SmartDashboard.getBoolean("Zero Toggle", false) == true){
      motor1.setSelectedSensorPosition(0);
      zeroToggle = false;
    }
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    SmartDashboard.putString("Current Auto:", m_autoSelected);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        
        motor1.set(ControlMode.PercentOutput, error * SmartDashboard.getNumber("Magic Number", magicNumber));

        break;
      case kFastAuto:

        if(error > boundsOfDegree){
          motor1.set(ControlMode.PercentOutput, -0.2);
        }else {
          if(error < -boundsOfDegree){
          motor1.set(ControlMode.PercentOutput, 0.2);
          }
        }


        break;
      case kCaseToCase:

        motor1.set(ControlMode.PercentOutput, error * SmartDashboard.getNumber("Magic Number", magicNumber));

      break;
      case kDefaultAuto:
      default:

      
        if (auto == 0){
        driveInDistance(SmartDashboard.getNumber("driveDistance", 5), 0.1);
        auto = 1;
        }
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    motor1.set(ControlMode.PercentOutput, joystick0.getRawAxis(1) * 0.25);

    if(joystick0.getRawButtonPressed(1)){
      motor1.setSelectedSensorPosition(0);
    }

  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {
    
    motor1.set(ControlMode.PercentOutput, 0);

  }

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
