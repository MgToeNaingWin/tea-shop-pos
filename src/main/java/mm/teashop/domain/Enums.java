package mm.teashop.domain;
public final class Enums {
  private Enums() {}
  public enum Area { INDOOR, OUTDOOR, PAVEMENT }
  public enum Category { DRINK, FOOD }
  public enum Station { BAR, KITCHEN }
  public enum OrderStatus { OPEN, SENT, PREPARING, READY, PAID, CANCELLED }
  public enum PaymentMethod { CASH, KBZPAY, WAVEPAY, AYAPAY, CBPAY }
  public enum StockMovementType { PURCHASE, ADJUSTMENT_IN, ADJUSTMENT_OUT, RECIPE_USAGE, WASTE }
}
