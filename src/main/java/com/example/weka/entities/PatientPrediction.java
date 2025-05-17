package com.example.weka.entities;

public class PatientPrediction {

  private int id;
  private String resultado; // benigno o maligno
  private String advice;

  private int clumpThickness;
  private int uniformityOfCellSize;
  private int uniformityOfCellShape;
  private int marginalAdhesion;
  private int singleEpithelialCellSize;
  private int bareNuclei;
  private int blandChromatin;
  private int normalNucleoli;
  private int mitoses;

  public PatientPrediction(int id, String resultado, String advice, PredictionRequest request) {
    this.id = id;
    this.resultado = resultado;
    this.advice = advice;

    this.clumpThickness = request.getClumpThickness();
    this.uniformityOfCellSize = request.getUniformityOfCellSize();
    this.uniformityOfCellShape = request.getUniformityOfCellShape();
    this.marginalAdhesion = request.getMarginalAdhesion();
    this.singleEpithelialCellSize = request.getSingleEpithelialCellSize();
    this.bareNuclei = request.getBareNuclei();
    this.blandChromatin = request.getBlandChromatin();
    this.normalNucleoli = request.getNormalNucleoli();
    this.mitoses = request.getMitoses();
  }

  public int getId() { return id; }
  public String getResultado() { return resultado; }
  public String getAdvice() { return advice; }

  public int getClumpThickness() { return clumpThickness; }
  public int getUniformityOfCellSize() { return uniformityOfCellSize; }
  public int getUniformityOfCellShape() { return uniformityOfCellShape; }
  public int getMarginalAdhesion() { return marginalAdhesion; }
  public int getSingleEpithelialCellSize() { return singleEpithelialCellSize; }
  public int getBareNuclei() { return bareNuclei; }
  public int getBlandChromatin() { return blandChromatin; }
  public int getNormalNucleoli() { return normalNucleoli; }
  public int getMitoses() { return mitoses; }
}

