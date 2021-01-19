package com.griddynamics.fusiontest;

import com.google.protobuf.ListValue;
import com.google.protobuf.Value;
import hex.genmodel.ModelMojoReader;
import hex.genmodel.MojoModel;
import hex.genmodel.MojoReaderBackend;
import hex.genmodel.MojoReaderBackendFactory;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.AbstractPrediction;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import io.seldon.protos.PredictionProtos;
import io.seldon.wrapper.utils.H2OUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//@SpringBootTest
class FusiontestApplicationTests {

    private EasyPredictModelWrapper getModel(String filename) throws IOException {
        MojoReaderBackend reader = MojoReaderBackendFactory.createReaderBackend(getClass().getClassLoader().getResourceAsStream(filename), MojoReaderBackendFactory.CachingStrategy.MEMORY);
        MojoModel modelMojo = ModelMojoReader.readFrom(reader);
        return new EasyPredictModelWrapper(modelMojo);
    }

    @Test
    public void testMojo() throws Exception {
        EasyPredictModelWrapper model = getModel("model.zip");
        RowData row = new RowData();
        row.put("AGE", "68");
        row.put("RACE", "2");
        row.put("DCAPS", "2");
        row.put("VOL", "0");
        row.put("GLEASON", "6");

        BinomialModelPrediction binomialModelPrediction = model.predictBinomial(row);
        System.out.println("Has penetrated the prostatic capsule (1=yes; 0=no): " + binomialModelPrediction.label);
        System.out.print("Class probabilities: ");
        for (int i = 0; i < binomialModelPrediction.classProbabilities.length; i++) {
            if (i > 0) {
                System.out.print(",");
            }
            System.out.print(binomialModelPrediction.classProbabilities[i]);
        }
        System.out.println("");
    }


    @Test
    public void testMojoFromTensor() throws IOException, PredictException {
        EasyPredictModelWrapper model = getModel("model.zip");

        io.seldon.protos.PredictionProtos.SeldonMessage msg = createSeldonMessageTensor();
        List<RowData> rows = H2OUtils.convertSeldonMessage(msg.getData());
        List<AbstractPrediction> predictions = new ArrayList<>();
        for (RowData row : rows) {
            BinomialModelPrediction p = model.predictBinomial(row);
            predictions.add(p);
        }
        PredictionProtos.DefaultData res = H2OUtils.convertH2OPrediction(predictions, msg.getData());
        System.out.println(res.toString());
    }

    @Test
    public void testMojoFromNDArray() throws IOException, PredictException {
        EasyPredictModelWrapper model = getModel("model.zip");
        PredictionProtos.SeldonMessage msg = createSeldonMessageNDArray();
        List<RowData> rows = H2OUtils.convertSeldonMessage(msg.getData());
        List<AbstractPrediction> predictions = new ArrayList<>();
        for (RowData row : rows) {
            BinomialModelPrediction p = model.predictBinomial(row);
            predictions.add(p);
        }
        PredictionProtos.DefaultData res = H2OUtils.convertH2OPrediction(predictions, msg.getData());
        System.out.println(res.toString());
    }

    private PredictionProtos.SeldonMessage createSeldonMessageTensor() {
        PredictionProtos.Tensor t = PredictionProtos.Tensor.newBuilder().addShape(1).addShape(5).addValues(68).addValues(2).addValues(2).addValues(0).addValues(6).build();

        PredictionProtos.SeldonMessage msg = PredictionProtos.SeldonMessage.newBuilder().setData(PredictionProtos.DefaultData.newBuilder().setTensor(t)
                .addNames("AGE")
                .addNames("RACE")
                .addNames("DCAPS")
                .addNames("VOL")
                .addNames("GLEASON")
        ).build();
        return msg;
    }

    private PredictionProtos.SeldonMessage createSeldonMessageNDArray() {
        ListValue row = ListValue.newBuilder()
                .addValues(Value.newBuilder().setNumberValue(68))
                .addValues(Value.newBuilder().setNumberValue(2))
                .addValues(Value.newBuilder().setNumberValue(2))
                .addValues(Value.newBuilder().setNumberValue(0))
                .addValues(Value.newBuilder().setNumberValue(6))
                .build();
        ListValue rows = ListValue.newBuilder().addValues(Value.newBuilder().setListValue(row)).build();
        PredictionProtos.SeldonMessage msg = PredictionProtos.SeldonMessage.newBuilder().setData(PredictionProtos.DefaultData.newBuilder().setNdarray(rows)
                .addNames("AGE")
                .addNames("RACE")
                .addNames("DCAPS")
                .addNames("VOL")
                .addNames("GLEASON")
        ).build();
        return msg;
    }
}
