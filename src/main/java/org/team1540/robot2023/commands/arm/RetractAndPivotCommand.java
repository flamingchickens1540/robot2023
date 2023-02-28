package org.team1540.robot2023.commands.arm;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class RetractAndPivotCommand extends SequentialCommandGroup {
    public RetractAndPivotCommand(Arm arm, Rotation2d setpoint) {
        addCommands(
                new ExtensionCommand(arm, 0),
                new PivotCommand(arm, setpoint)
        );
        addRequirements(arm);
    }
}