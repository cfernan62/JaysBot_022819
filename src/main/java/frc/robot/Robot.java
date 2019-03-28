/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import edu.wpi.first.wpilibj.Joystick;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.cameraserver.CameraServer;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot
{
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  TalonSRX RightMotor = new TalonSRX(0);
  VictorSPX LeftMotor = new VictorSPX(1);

  private static final int kJoystickPort = 0;
  private Joystick m_joystick;
    final double ROBOT_MAX_SPEED = 1.0;
  final double ROBOT_NORMAL_SPEED = 0.7;
  final double ROBOT_MAX_TURNSPEED = 0.45;  
  double RobotActualSpeed;

  AnalogInput TankPressure = new AnalogInput(0);

  final int CountDown = 150 * 50;        // convert seconds to 20msec increments        
  final int TargetCountDown = 40 * 50;   // signal driver when countdown is 40 seconds.
  int countdown_counter;
  boolean climb_now = false;
  
  double PressureVolts;
  Scurve sobj = new Scurve(RightMotor, LeftMotor);

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() 
  {
    SmartDashboard.putString("Mode", "Robot Init" );
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    RightMotor.configOpenloopRamp(2);
    LeftMotor.configOpenloopRamp(2);

    RightMotor.set(ControlMode.PercentOutput, 0);
    LeftMotor.set(ControlMode.PercentOutput, 0);
    m_joystick = new Joystick(kJoystickPort);

    CameraServer.getInstance().startAutomaticCapture();
  
  }


  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() 
  {
     if( countdown_counter > 0 )
       countdown_counter--;

     if( countdown_counter < TargetCountDown )
       climb_now = true;
     else
       climb_now = false;
  }


  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() 
  {
    SmartDashboard.putString("Mode", "Autonomous Init" );
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    countdown_counter = CountDown;
  }


  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() 
  {
    switch (m_autoSelected) 
    {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }


  @Override
  public void teleopInit() 
  {
    SmartDashboard.putString("Mode", "TeleOp Init" );
    super.teleopInit();
  }


  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() 
  {
    /* Gamepad processing */
   // double forward = -1 * m_joystick.getY();
   // double turn = m_joystick.getX();
    double forward = -1 * m_joystick.getRawAxis(5);
    double turn = m_joystick.getRawAxis(4);

    forward = Deadband(forward);
    turn = Deadband(turn);

    if( m_joystick.getRawButton(1))
      RobotActualSpeed = ROBOT_MAX_SPEED;
    else
      RobotActualSpeed = ROBOT_NORMAL_SPEED;
     
    //restricting forward speed to robot max
    if (forward > ROBOT_MAX_SPEED )
      forward = ROBOT_MAX_SPEED;
    else if (forward < -ROBOT_MAX_SPEED)
      forward = -ROBOT_MAX_SPEED;
    
    
    forward = forward * RobotActualSpeed;

    //restricting turn speed to robot max
    if (turn > ROBOT_MAX_TURNSPEED )
      turn = ROBOT_MAX_TURNSPEED;
    else if (turn < -ROBOT_MAX_TURNSPEED)
      turn = -ROBOT_MAX_TURNSPEED; 
         
    turn = turn * RobotActualSpeed;

    /* Arcade Drive using PercentOutput along with Arbitrary Feed Forward supplied by turn */
    RightMotor.set(ControlMode.PercentOutput, forward,  DemandType.ArbitraryFeedForward, +turn);
    LeftMotor.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
    
    PressureVolts = TankPressure.getVoltage();
    SmartDashboard.putNumber("PressureVolts", PressureVolts);
  }


  @Override
  public void testInit() 
  {
    SmartDashboard.putString("Mode", "Test Init" );
    super.testInit();
  }


  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() 
  {
    sobj.scurve_move();

  }


  /** Deadband 8 percent, used on the gamepad */
  double Deadband(double value) 
  {   
    if (value >= +0.1)  // Upper Deadband 
      return value;
      
    if (value <= -0.1)  // Lower Deadband
      return value;
   
    return 0;           // Outside Deadband
  }
}
