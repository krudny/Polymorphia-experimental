import { AssignmentStrategy } from "@/components/speed-dial/strategies/markdown-view/Assignment";
import { ProjectStrategy } from "@/components/speed-dial/strategies/markdown-view/Project";
import { SpeedDialStrategy } from "@/components/speed-dial/strategies/types";
import { TestGradingStrategy } from "@/components/speed-dial/strategies/grading/TestGrading";
import { AssignmentGradingStrategy } from "@/components/speed-dial/strategies/grading/AssignmentGrading";
import { SpeedDialKey, SpeedDialKeys } from "@/components/speed-dial/types";
import { CourseGroupStrategy } from "@/components/speed-dial/strategies/course-groups/CourseGroup";
import { RulesStrategy } from "@/components/speed-dial/strategies/markdown-view/Rules";
import { ProfileStrategy } from "@/components/speed-dial/strategies/student/Profile";
import { CourseGroupGridStrategy } from "@/components/speed-dial/strategies/course-groups/CourseGroupGrid";
import { ProjectGradingStrategy } from "@/components/speed-dial/strategies/grading/ProjectGrading";

export class SpeedDialStrategyRegistry {
  private strategies = new Map<SpeedDialKey, SpeedDialStrategy>();

  constructor() {
    this.strategies.set(
      SpeedDialKeys.ASSIGNMENT_MARKDOWN,
      new AssignmentStrategy()
    );
    this.strategies.set(SpeedDialKeys.PROJECT_MARKDOWN, new ProjectStrategy());
    this.strategies.set(SpeedDialKeys.RULES_MARKDOWN, new RulesStrategy());
    this.strategies.set(SpeedDialKeys.TEST_GRADING, new TestGradingStrategy());
    this.strategies.set(SpeedDialKeys.PROFILE_STUDENT, new ProfileStrategy());
    this.strategies.set(
      SpeedDialKeys.ASSIGNMENT_GRADING,
      new AssignmentGradingStrategy()
    );
    this.strategies.set(
      SpeedDialKeys.PROJECT_GRADING,
      new ProjectGradingStrategy()
    );
    this.strategies.set(SpeedDialKeys.COURSE_GROUP, new CourseGroupStrategy());
    this.strategies.set(
      SpeedDialKeys.COURSE_GROUP_GRID,
      new CourseGroupGridStrategy()
    );
  }

  getStrategy(speedDialKey: SpeedDialKey): SpeedDialStrategy | null {
    if (!speedDialKey) {
      return null;
    }

    return this.strategies.get(speedDialKey) || null;
  }
}

export const speedDialStrategyRegistry = new SpeedDialStrategyRegistry();
