package timeline;
import timeline.TimelinedataFormatter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import timeline.*;
import timeline.Variable;

public class TimelineExporter {
    public static void export(TimelineData timeline, String filename) throws FileNotFoundException {
        Goal g = buildAST(timeline);
        TimelinedataFormatter formatter = new TimelinedataFormatter();
        TreeDumper treeDumper = new TreeDumper(new FileOutputStream(filename, false));
        g.accept(formatter);
        g.accept(treeDumper);
    }

    private static Goal buildAST(TimelineData timeline) {
        // build option list
        NodeList optionNodeList = new NodeList();

        if (timeline.playbackMode == TimelineData.PlaybackType.TIME_BASED) {
            optionNodeList.addNode(new Option(new PlaybackChoice(new NodeChoice(new PlaybackChoiceTime()))));
        } else if (timeline.playbackMode == TimelineData.PlaybackType.FRAME_BASED) {
            optionNodeList.addNode(new Option(new PlaybackChoice(new NodeChoice(new PlaybackChoiceFrames(new IntegerLiteral(new NodeToken("" + timeline.fps)))))));
        }

        OptionList optionList = new OptionList(optionNodeList);

        // build the variables
        NodeListOptional variableList = new NodeListOptional();
        for (TimelineVariable timelineVariable : timeline.timelineVariables) {
            Identifier identifier = new Identifier(new NodeToken(timelineVariable.name));
            NodeList vertices = new NodeList();

            // build the vertices
            for (BezierVertex v : timelineVariable.spline.vertices) {
                FloatingPointLiteral fp0 = new FloatingPointLiteral(new NodeToken(Float.toString(v.a.x)));
                FloatingPointLiteral fp1 = new FloatingPointLiteral(new NodeToken(Float.toString(v.a.y)));
                FloatingPointLiteral fp2 = new FloatingPointLiteral(new NodeToken(Float.toString(v.t1.x)));
                FloatingPointLiteral fp3 = new FloatingPointLiteral(new NodeToken(Float.toString(v.t1.y)));
                FloatingPointLiteral fp4 = new FloatingPointLiteral(new NodeToken(Float.toString(v.t2.x)));
                FloatingPointLiteral fp5 = new FloatingPointLiteral(new NodeToken(Float.toString(v.t2.y)));

                Vertex vertex = new Vertex(fp0, fp1, fp2, fp3, fp4, fp5);
                vertices.addNode(vertex);
            }

            Variable variable = new Variable(identifier, vertices);
            variableList.addNode(variable);
        }

        // build the goal (root of tree)
        Goal goal = new Goal(new NodeOptional(optionNodeList), variableList);

        return goal;
    }
}
