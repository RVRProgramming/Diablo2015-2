package org.usfirst.frc.team87.robot;

import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc.team87.robot.commands.ExampleCommand;
import org.usfirst.frc.team87.robot.subsystems.ExampleSubsystem;

//import com.sun.org.apache.xalan.internal.xsltc.trax.SmartTransformerFactoryImpl;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {

	// TODO GET LIMIT SWITCH PORTS FROM CARL

	RobotDrive robotDrive; // Robot Drive
	Relay leftRoll, rightRoll; // Rollers
	Victor leftLift, rightLift; // Lift motors (windows)
	Joystick gamePad, extreme3D; // Logitech gamepad and Extreme3D Pro
									// (Joystick)
	Solenoid leftGrabExtend, rightGrabExtend, leftGrabRetract,
			rightGrabRetract; // Two solenoids for flaps
	DigitalInput leftMax, rightMax, leftMin, rightMin, leftTote, rightTote,
			autoSwitch; // Limit switches & Autonomous Switch
	// (Left side max, right side max, left side min, right side min, do we have
	// a tote?)

	Counter leftEncoder;
	Counter rightEncoder;

	boolean grab = false;
	boolean atTop = false;
	boolean atBot = false;

	public static final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();
	public static OI oi;

	public SmartDashboard SmartDashboard = new SmartDashboard();

	Command autonomousCommand;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit() {
		oi = new OI();
		// instantiate the command used for the autonomous period
		this.autonomousCommand = new ExampleCommand();

		this.robotDrive = new RobotDrive(9, 8, 0, 1); // ROBOT DRIVE PWM 1, 2,
														// 3, 4);

		this.leftRoll = new Relay(0);
		this.rightRoll = new Relay(1);

		this.leftLift = new Victor(7);
		this.rightLift = new Victor(2);

		this.gamePad = new Joystick(0); // GAMEPAD IS JOYSTICK 0
		this.extreme3D = new Joystick(1); // EXTREME IS JOYSTICK 1

		this.leftGrabExtend = new Solenoid(5); // EXTEND LEFT FLAP SOLENOID
		this.leftGrabRetract = new Solenoid(2); // RETRACT LEFT FLAP SOLENOID
		this.rightGrabExtend = new Solenoid(3); // EXTEND RIGHT FLAP SOLENOID
		this.rightGrabRetract = new Solenoid(4); // RETRACT RIGHT FLAP SOLENOID

		this.leftMax = new DigitalInput(9); // LEFT MAX LIMITSWITCH
		this.rightMax = new DigitalInput(0);// RIGHT MAX LIMITSWITCH

		this.leftMin = new DigitalInput(8); // LEFT MIN LIMITSWITCH
		this.rightMin = new DigitalInput(1);// RIGHT MIN LIMITSWITCH

		this.leftTote = new DigitalInput(7);// TOTE STAT FOR LEFT LIMITSWITCH
		this.rightTote = new DigitalInput(2);// TOTE STAT FOR RIGHT LIMITSWITCH

		this.autoSwitch = new DigitalInput(4);// AUTO SWITCH

		this.leftEncoder = new Counter(6);
		this.rightEncoder = new Counter(3);

		// Port Declarations

		// Digital I/O
		// 0 Right Lower Lift Limit
		// 1 Right Upper Lift Limit
		// 2 Right Tote limit
		// 3 Right Rotary Encoder
		// 4 Empty
		// 5 Empty
		// 6 Left Rotary Encoder
		// 7 Left Tote limit
		// 8 Left Upper Lift Limit
		// 9 Left Lower Lift Limit

		// PWM
		// 0 Right CIM
		// 1 RIght CIM
		// 2 RIght Lift
		// 3 Empty
		// 4 Empty
		// 5 Empty
		// 6 Empty
		// 7 Left Lift
		// 8 Left CIM
		// 9 Left CIM

		// Relay
		// Left Tote Motor
		// Right Tote Motor

	}

	public void disabledPeriodic() {
		try {
			if (this.SmartDashboard.getBoolean("DB/Button 0")) {
				this.SmartDashboard.putString("DB/String 0",
						"Smart Dashboard found");
				this.SmartDashboard.putString("DB/String 1", "Button 0 true");
			} else {
				this.SmartDashboard.putString("DB/String 0",
						"Smart Dashboard found");
				this.SmartDashboard.putString("DB/String 1", "Button 0 false");
			}
		} catch (Exception e) {
			// System.err.println(e.getStackTrace());
		}
		Scheduler.getInstance().run();
	}

	public void autonomousInit() {
		// schedule the autonomous command (example)

		this.leftEncoder.reset();
		this.rightEncoder.reset();
		
		if (this.autonomousCommand != null)
			this.autonomousCommand.start();
		
	}

	/**
	 * This function is called periodically during autonomous
	 */
	// Time the robot will go forward

	public final double FLAT_TIME = 2.25;
	public final double BUMP_TIME = 2.5;
	public double FINAL_TIME = 2.25;
	public boolean moveToteAuto = false;
	public boolean finalState = false;
	public boolean firstState = true;
	public double finalDist = 1.9;
	public int moveToteAutoState = -1;

	Timer autoTimer;

	public void autonomousPeriodic() {

		System.out.println("Auto State " + moveToteAutoState);

		double speed;
		Scheduler.getInstance().run();

		this.SmartDashboard.putString("DB/String 0", "Autonomous");

		try {
			if (this.SmartDashboard.getBoolean("DB/Button 0"))
				;
			{
				this.FINAL_TIME = 1.9;
				this.finalDist = 3700;
			}
			if (!this.SmartDashboard.getBoolean("DB/Button 0")
					&& !this.SmartDashboard.getBoolean("DB/Button 1")) {
				this.FINAL_TIME = 1.7;
				this.finalDist = 3300;
			}

			if (this.SmartDashboard.getBoolean("DB/Button 1")) {
				
					if(this.SmartDashboard.getBoolean("DB/Button 0"))
					{
						this.finalDist = 1.9;
						this.SmartDashboard.putString("DB/String 5", "LONG");

					}
					else
					{
						this.SmartDashboard.putString("DB/String 5", "SHORT");
						this.finalDist = 1.75;
					}
				this.FINAL_TIME = 0;
				this.moveToteAuto = true;
			}
			
			if (this.SmartDashboard.getBoolean("DB/Button 2"))
			{
				this.FINAL_TIME = 1;
			}

			if (this.SmartDashboard.getBoolean("DB/Button 3"))
			{
				this.FINAL_TIME = 0;
			}
			
		} catch (Exception x) {
			this.FINAL_TIME = 1.8;

		}

		speed = 0;

		// Creates a new timer if one does not exist
		if (this.autoTimer == null) {
			this.autoTimer = new Timer();
			this.autoTimer.reset();
			this.autoTimer.start();
		}
		// If timer is less than forward time, set speed to one
		if (this.autoTimer.get() < this.FINAL_TIME) {
			speed = 1;
		} else {
			speed = 0;
		}
		// sets robot tank drive to speed
		this.robotDrive.tankDrive(speed, speed);

		if (this.moveToteAuto) {
			if ((this.leftMax.get() || this.rightMax.get())
					&& this.moveToteAutoState == 0) {

				this.leftGrabRetract.set(false);// Stop retracting left piston
				this.rightGrabRetract.set(false);// Stop retracting right piston
				this.leftGrabExtend.set(true); // Extend left piston
				this.rightGrabExtend.set(true); // Extend right piston

				// Checks that stick is pulled and left lift is not at top
				if (this.leftMax.get()) {
					this.leftLift.set(1);
					this.lowering = false;
					this.lifting = false;
				} else {
					this.leftLift.set(0);
				}
				// Checks that stick is pulled and right lift is not at top
				if (this.rightMax.get()) {
					this.rightLift.set(1);
					this.lowering = false;
					this.lifting = false;
				} else {
					this.rightLift.set(0);
				}
			}

			if (moveToteAutoState == -1
					&& ((!this.leftTote.get() && !this.rightTote.get()) || (this.autoTimer
							.get() > 1))) {
				this.leftRoll.set(Relay.Value.kOff);
				this.rightRoll.set(Relay.Value.kOff);
				System.out.println("State to 0");
				moveToteAutoState = 0;
				autoTimer.reset();
			}

			if (!this.leftMax.get() && !this.rightMax.get() && this.firstState == true) {
				System.out.println("Entering 0 to 1");
				this.moveToteAutoState = 1;
				this.autoTimer = null;
				this.firstState = false;
				this.leftGrabRetract.set(true);// Stop retracting left piston
				this.rightGrabRetract.set(true);// Stop retracting right piston
				this.leftGrabExtend.set(false); // Extend left piston
				this.rightGrabExtend.set(false); // Extend right piston
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}

			// Drive into tote
			if (this.moveToteAutoState == -1) {

				if (this.leftTote.get()) {
					this.leftRoll.set(Relay.Value.kForward);
				} else {
					this.leftRoll.set(Relay.Value.kOff);
				}

				if (this.rightTote.get()) {
					this.rightRoll.set(Relay.Value.kReverse);
				} else {
					this.rightRoll.set(Relay.Value.kOff);
				}

				this.robotDrive.tankDrive(.5, .5);

			}// End of Drive into Tote

			// Lift Tote
			if (this.moveToteAutoState == 1 && !this.leftTote.get()
					&& !this.rightTote.get()) {
				if (this.autoTimer == null) {
					this.autoTimer = new Timer();
					this.autoTimer.reset();
					this.autoTimer.start();

				}

				this.leftGrabRetract.set(true);// Extent left piston
				this.rightGrabRetract.set(true);// Extend right piston
				this.leftGrabExtend.set(false); // Extend left piston
				this.rightGrabExtend.set(false); // Extend right piston

				while (this.autoTimer.get() <= .5 && moveToteAutoState == 1) {
					// Checks that stick is pulled and left lift is not at top
					if (this.leftMin.get()) {
						this.leftLift.set(-1);
						this.lowering = false;
						this.lifting = false;
					} else {
						this.leftLift.set(0);
					}

					// Checks that stick is pulled and right lift is not at top
					if (this.rightMin.get()) {
						this.rightLift.set(-1);
						this.lowering = false;
						this.lifting = false;
					} else {
						this.rightLift.set(0);
					}
				}

			} // End lifting tote
try{
			if (this.autoTimer.get() >= .5 && this.moveToteAutoState == 1) {
				this.leftLift.set(0);
				this.rightLift.set(0);
				this.moveToteAutoState = 3; // CHANGE TO 2 TO REINSTATE BACKUP
				this.autoTimer = null;
				this.leftEncoder.reset();
				this.rightEncoder.reset();
			} // REMOVED BACKUP, CHNAGE moveToteAutoState to 2 to insert backup again. 
}

// Catches exception thrown if the timer is set to null. This can happen when no totes
// are in the robot after the timer in the drive forward to take in a tote runs out.
// This is a fail safe
catch (Exception z)
{
	this.autoTimer = new Timer();
	this.autoTimer.reset();
	this.autoTimer.start();
	this.finalState = true;
	this.moveToteAutoState = 6;
}
			// Reversing bot DOES NOT WORK SEE ABOVE

// Check to see if we are in state 2, reverse state. Reverse for 100 encoder counts on each wheel
			if (this.moveToteAutoState == 2
					&& (this.leftEncoder.get() < 100 || this.rightEncoder
							.get() < 100)) {
				this.robotDrive.tankDrive(-1, -1);
				this.leftRoll.set(Relay.Value.kOff);
				this.rightRoll.set(Relay.Value.kOff);
			}
// Set state to 3 and reset encoders once we hit 100 encoder counts on each wheel
			if (this.moveToteAutoState == 2 && this.leftEncoder.get() >= 100
					&& this.rightEncoder.get() >= 100) {
				this.robotDrive.tankDrive(0, 0);
				this.leftEncoder.reset();
				this.rightEncoder.reset();
				this.moveToteAutoState = 3;

			} // End reversing bot

			// Turning bot
// Check to see if we are in state 3, turn state, and turn each wheen for 420 encoder counts
// This makes the bot turn right
			
			if (this.moveToteAutoState == 3 && (this.leftEncoder.get() < 420
					|| this.rightEncoder.get() < 420)) {
				this.robotDrive.tankDrive(.7, -.7);
			}

			if (this.moveToteAutoState == 3 && this.leftEncoder.get()
					>= 420 && this.rightEncoder.get() >= 420)
			{
				this.robotDrive.tankDrive(0, 0);
				this.leftEncoder.reset();
				this.rightEncoder.reset();
				this.moveToteAutoState = 4;
				this.autoTimer.reset();
			}
			
			if (this.moveToteAutoState == 4 && this.autoTimer.get() < this.finalDist) {
				this.robotDrive.tankDrive(1, 1);
			}

			if (this.moveToteAutoState == 4 && this.autoTimer.get() >= this.finalDist){
				this.robotDrive.tankDrive(0, 0);
				this.leftEncoder.reset();
				this.rightEncoder.reset();
				this.moveToteAutoState = 5;

			}// End of drive forward

			if (this.moveToteAutoState == 5) {
				if (this.leftMax.get()) {
					this.leftLift.set(1);
					this.lowering = false;
					this.lifting = false;
				}
				else
				{
					this.leftLift.set(0);
				}
				// Checks that stick is pulled and right lift is not at top
				if (this.rightMax.get()) {
					this.rightLift.set(1);
					this.lowering = false;
					this.lifting = false;
				}
				else
				{
					this.rightLift.set(0);
				}

				if (!this.rightMax.get() && !this.leftMax.get()) {
					this.finalState = true;
					this.leftGrabRetract.set(false);// Stop retracting left piston
					this.rightGrabRetract.set(false);// Stop retracting right piston
					this.leftGrabExtend.set(true); // Extend left piston
					this.rightGrabExtend.set(true); // Extend right piston

				}
			}

			// Spit out the tote
			if (this.finalState) {
				this.leftRoll.set(Relay.Value.kReverse);
				this.rightRoll.set(Relay.Value.kForward);
			}

			} // End turning bot

			// Drive forward (NEED TO CHANGE TO TIMER
			

		} // End of move tote



	public void teleopInit() {
		// This makes sure that the autonomous stops running when
		// teleop starts running. If you want the autonomous to
		// continue until interrupted by another command, remove
		// this line or comment it out.
		if (this.autonomousCommand != null)
			this.autonomousCommand.cancel();
	}

	/**
	 * This function is called when the disabled button is hit. You can use it
	 * to reset subsystems before shutting down.
	 */
	public void disabledInit() {

	}

	/**
	 * This function is called periodically during operator control
	 */
	public boolean lowering = false;
	public boolean lifting = false;
	Timer liftTimer;

	public void teleopPeriodic() {
		Scheduler.getInstance().run();

		System.out.println("Left En " + this.leftEncoder.get());
		System.out.println("Right En " + this.rightEncoder.get());

		if (this.liftTimer == null) {
			this.liftTimer = new Timer();
		}
		double leftDrive = this.gamePad.getRawAxis(1) * -1;
		double rightDrive = this.gamePad.getRawAxis(5) * -1;

		this.robotDrive.tankDrive(leftDrive * .7, rightDrive * .7);

		// If button 1 is pressed , drop totes / retract flaps
		if (this.gamePad.getRawButton(5) && this.gamePad.getRawButton(6)) {
			this.robotDrive.tankDrive(leftDrive, rightDrive);
		}
		if (this.extreme3D.getRawButton(1) == true) {
			this.leftGrabRetract.set(false);// Stop retracting left piston
			this.rightGrabRetract.set(false);// Stop retracting right piston
			this.leftGrabExtend.set(true); // Extend left piston
			this.rightGrabExtend.set(true); // Extend right piston
			this.lowering = false;
			this.lifting = false;
		} else { // Extends flaps when trigger is not pressed
			this.leftGrabRetract.set(true);// Retract left piston
			this.rightGrabRetract.set(true);// Retract right piston
			this.leftGrabExtend.set(false);// Stop extending left piston
			this.rightGrabExtend.set(false);// Stop extending right piston
		}

		double liftVal = (this.extreme3D.getRawAxis(1)); // Get extreme3D Pro
															// joystick axis

		// Checks that stick is pulled and left lift is not at top
		if (liftVal > 0.4 && (this.leftMin.get())) {
			this.leftLift.set(-1);
			this.lowering = false;
			this.lifting = false;
		}
		// Checks that stick is puched and left lift is not at bottom
		else if (liftVal < -0.4 && (this.leftMax.get())) {
			this.leftLift.set(1);
			this.lowering = false;
			this.lifting = false;
		}
		// Stops left elevator
		else {
			this.leftLift.set(0);
		}
		// Checks that stick is pulled and right lift is not at top
		if (liftVal > 0.4 && (this.rightMin.get())) {
			this.rightLift.set(-1);
			this.lowering = false;
			this.lifting = false;
		}
		// Checks that stick is puched and right lift is not at bottom
		else if (liftVal < -0.4 && (this.rightMax.get())) {
			this.rightLift.set(1);
			this.lowering = false;
		}
		// Stops left elevator
		else {
			this.rightLift.set(0);
		}

		if (this.extreme3D.getRawButton(5) && this.lowering != true) {// Jimmy
																		// button
			this.lowering = true;
			this.lifting = false;
		}

		if (this.extreme3D.getRawButton(6) && this.lifting != true) {
			this.lifting = true;
			this.lowering = false;
		}

		if (this.lowering) {
			if (this.leftMax.get()) {
				this.leftLift.set(1);
			} else {
				this.leftLift.set(0);
			}
			if (this.rightMax.get()) {
				this.rightLift.set(1);
			} else {
				this.rightLift.set(0);
			}
			if(!this.leftMax.get() && !this.rightMax.get())
			{
				this.lowering = false;
			}
			
		} else {
			this.lowering = false;
		}
		if (this.lifting) {
			if (this.leftMin.get()) {
				this.leftLift.set(-1);
			} else {
				this.leftLift.set(0);
			}
			if (this.rightMin.get()) {
				this.rightLift.set(-1);
			} else {
				this.rightLift.set(0);
			}
			if (!this.leftMin.get() && !this.rightMin.get())
			{
				this.lifting = false;
			}
			
		} else {
			this.lifting = false;
		}

		if (this.extreme3D.getRawButton(3)) {
			if (this.leftTote.get()) {
				this.leftRoll.set(Relay.Value.kForward);
			} else {
				this.leftRoll.set(Relay.Value.kOff);
			}

			if (this.rightTote.get()) {
				this.rightRoll.set(Relay.Value.kReverse);
			} else {
				this.rightRoll.set(Relay.Value.kOff);
			}
		}

		else if (this.extreme3D.getRawButton(4)) {
			this.leftRoll.set(Relay.Value.kReverse);
			this.rightRoll.set(Relay.Value.kForward);
		}

		else {
			this.leftRoll.set(Relay.Value.kOff);
			this.rightRoll.set(Relay.Value.kOff);
		}

	}

	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic() {
		LiveWindow.run();
	}
}
