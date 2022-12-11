/*
 * Central Ledger
 * These are the endpoints provided by the Central Ledger server of CAPTOR. These endpoints are used by the Inspect and Transport applications. The Inspect application will use the endpoints with inspection tags and the Transport application will use the endpoints with trip tags
 *
 * OpenAPI spec version: 2.0.1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;

/**
 * Product
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2019-04-24T14:07:08.201Z[GMT]")public class Product   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("quantity")
  private Long quantity = null;

  @JsonProperty("weight")
  private Long weight = null;

  public Product name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   **/
  @JsonProperty("name")
  @Schema(description = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Product quantity(Long quantity) {
    this.quantity = quantity;
    return this;
  }

  /**
   * Get quantity
   * @return quantity
   **/
  @JsonProperty("quantity")
  @Schema(description = "")
  public Long getQuantity() {
    return quantity;
  }

  public void setQuantity(Long quantity) {
    this.quantity = quantity;
  }

  public Product weight(Long weight) {
    this.weight = weight;
    return this;
  }

  /**
   * Get weight
   * @return weight
   **/
  @JsonProperty("weight")
  @Schema(description = "")
  public Long getWeight() {
    return weight;
  }

  public void setWeight(Long weight) {
    this.weight = weight;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Product product = (Product) o;
    return Objects.equals(this.name, product.name) &&
        Objects.equals(this.quantity, product.quantity) &&
        Objects.equals(this.weight, product.weight);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, quantity, weight);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Product {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
    sb.append("    weight: ").append(toIndentedString(weight)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
