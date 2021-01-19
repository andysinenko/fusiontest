package com.griddynamics.fusiontest.model;

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
import io.seldon.wrapper.api.SeldonPredictionService;
import io.seldon.wrapper.utils.H2OUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class H2OModelHandler implements SeldonPredictionService {
    private static Logger logger = LoggerFactory.getLogger(H2OModelHandler.class.getName());
    EasyPredictModelWrapper model;

    public H2OModelHandler() throws IOException {
        MojoReaderBackend reader = MojoReaderBackendFactory.createReaderBackend(getClass().getClassLoader().getResourceAsStream("model.zip"), MojoReaderBackendFactory.CachingStrategy.MEMORY);
        MojoModel modelMojo = ModelMojoReader.readFrom(reader);
        model = new EasyPredictModelWrapper(modelMojo);
        logger.info("Loaded model");
    }

    @Override
    public PredictionProtos.SeldonMessage predict(PredictionProtos.SeldonMessage payload) {
        List<RowData> rows = H2OUtils.convertSeldonMessage(payload.getData());
        List<AbstractPrediction> predictionArrayList = new ArrayList<>();
        for (RowData row : rows) {
            try {
                BinomialModelPrediction p = model.predictBinomial(row);
                predictionArrayList.add(p);
            } catch (PredictException e) {
                logger.info("Error in prediction ", e);
            }
        }
        PredictionProtos.DefaultData res = H2OUtils.convertH2OPrediction(predictionArrayList, payload.getData());

        return PredictionProtos.SeldonMessage.newBuilder().setData(res).build();
    }

}
