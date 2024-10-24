package service;

import controller.ProductController;
import model.ProductModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.File;

public class ProductManager {
    private ProductController productController;
    private static final String BASE_IMAGE_PATH = "resources/images/products/";
    private List<ProductModel> products;

    public ProductManager() {
        this.productController = new ProductController();
        this.products = new ArrayList<>();
        loadProducts(); // Phương thức để tải sản phẩm từ cơ sở dữ liệu
    }

    private void loadProducts() {
        this.products = productController.getAllProducts();
    }

    public synchronized ProductModel getRandomProduct() {
        if (products.isEmpty()) {
            return null;
        }
        int randomIndex = (int) (Math.random() * products.size());
        return products.get(randomIndex);
    }

    public synchronized void addProduct(ProductModel product) {
        String result = productController.addProduct(product);
        if (result.startsWith("success")) {
            products.add(product);
        }
    }

    public synchronized void removeProduct(int id) {
        String result = productController.deleteProduct(id);
        if (result.startsWith("success")) {
            products.removeIf(product -> product.getId() == id);
        }
    }

    

    public void updateProduct(ProductModel product) {
        String result = productController.updateProduct(product);
        if (result.startsWith("success")) {
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId() == product.getId()) {
                    products.set(i, product);
                    break;
                }
            }
        }
    }

    public List<ProductModel> getAllProducts() {
        return new ArrayList<>(products);
    }

    public ProductModel getProduct(int id) {
        return productController.getProduct(id);
    }
    
    public String getFullImagePath(String imageName) {
        return BASE_IMAGE_PATH + imageName;
    }

    public boolean imageExists(String imageName) {
        File file = new File(getFullImagePath(imageName));
        return file.exists();
    }
}
