package io.github.blueprintplatform.samples.customerservice.client.customer;

public enum CustomerSortField {
  CUSTOMER_ID("customerId"),
  NAME("name"),
  EMAIL("email");

  private final String value;

  CustomerSortField(String value) {
    this.value = value;
  }

  public static CustomerSortField from(String s) {
    if (s == null) return CUSTOMER_ID;
    for (var f : values()) {
      if (f.value.equalsIgnoreCase(s)) return f;
    }
    throw new IllegalArgumentException("Unsupported sort field: " + s);
  }

  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
