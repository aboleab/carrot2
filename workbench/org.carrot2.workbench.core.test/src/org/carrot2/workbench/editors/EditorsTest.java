package org.carrot2.workbench.editors;

import org.carrot2.core.ClusteringAlgorithm;
import org.carrot2.core.ProcessingComponentBase;
import org.carrot2.core.attribute.Processing;
import org.carrot2.util.attribute.*;
import org.carrot2.util.attribute.constraint.DoubleRange;
import org.carrot2.util.attribute.constraint.IntRange;

public class EditorsTest
{
    public static class TestType
    {
    }

    public static class TestType2
    {
    }

    @Bindable
    public static class TestComponent extends ProcessingComponentBase implements
        ClusteringAlgorithm
    {

        @Attribute
        @Input
        @Processing
        TestType specificAttribute;

        @Attribute
        @Input
        @Processing
        @IntRange(min = 0, max = 10)
        TestType rangeAttribute;

        @Attribute
        @Input
        @Processing
        TestType simpleAttribute;

        @Attribute
        @Input
        @Processing
        @DoubleRange(min = 0, max = 10)
        TestType2 rangeDoubleAttribute;
    }

    public static class DedicatedEditor extends AttributeEditorAdapter
    {

    }

    public static class DedicatedEditor2 extends AttributeEditorAdapter
    {

    }

    public static class DedicatedEditor3 extends AttributeEditorAdapter
    {

    }

    public static class DedicatedEditor4 extends AttributeEditorAdapter
    {

    }

    public static class RangeEditor extends AttributeEditorAdapter
    {

    }

    public static class IntEditor extends AttributeEditorAdapter
    {

    }

    public static class RangeEditor2 extends AttributeEditorAdapter
    {

    }

}
