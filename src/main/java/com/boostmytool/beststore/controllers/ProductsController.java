package com.boostmytool.beststore.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.boostmytool.beststore.models.Products;
import com.boostmytool.beststore.models.ProductDto;
import com.boostmytool.beststore.models.Products.Status;
import com.boostmytool.beststore.services.ProductsRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductsController {

    private final ProductsRepository repo;

    @Autowired
    public ProductsController(ProductsRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/Createproduct";
    }

    @PostMapping("/create")
    public String createProduct(
        @Valid @ModelAttribute ProductDto productDto,
        BindingResult result, 
        @RequestParam("imageFile") MultipartFile imageFile,
        Model model) {

        if (imageFile.isEmpty()) {
            result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
        }
        
        if (result.hasErrors()) {
            return "products/Createproduct";
        }

        String storageFileName = null;
        try {
            if (!imageFile.isEmpty()) {
                storageFileName = saveImageFile(imageFile);
            }
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            return "redirect:/products";
        }

        Products product = new Products();
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setDiscountPrice(productDto.getDiscountPrice());
        product.setCategory(productDto.getCategory());
        product.setStatus(Status.AVAILABLE);  // Default status
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(new Date());
        product.setImageFileName(storageFileName);

        repo.save(product);

        return "redirect:/products";
    }

    private String saveImageFile(MultipartFile image) throws IOException {
        String storageFileName = new Date().getTime() + "_" + image.getOriginalFilename();
        String uploadDir = "public/Image/";  // Ensure this path is correct

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (var inputStream = image.getInputStream()) {
            Files.copy(inputStream, uploadPath.resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
        }

        return storageFileName;
    }

    @GetMapping("/edit")
    public String showEditPage(
        Model model,
        @RequestParam Long id) {
        
        Optional<Products> optionalProduct = repo.findById(id);
        if (optionalProduct.isPresent()) {
            Products product = optionalProduct.get();
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setPrice(product.getPrice());
            productDto.setDiscountPrice(product.getDiscountPrice());
            productDto.setCategory(product.getCategory()); // Directly set the enum value
            productDto.setDescription(product.getDescription());

            model.addAttribute("productDto", productDto);
            return "products/EditProduct";
        } else {
            System.out.println("Product not found with ID: " + id);
            return "redirect:/products";
        }
    }

    @PostMapping("/edit")
    public String updateProduct(
        @RequestParam Long id,
        @Valid @ModelAttribute ProductDto productDto,
        BindingResult result, 
        @RequestParam("imageFile") MultipartFile imageFile,
        Model model) {

        if (result.hasErrors()) {
            return "products/EditProduct";
        }

        Optional<Products> optionalProduct = repo.findById(id);
        if (optionalProduct.isPresent()) {
            Products product = optionalProduct.get();
            product.setName(productDto.getName());
            product.setPrice(productDto.getPrice());
            product.setDiscountPrice(productDto.getDiscountPrice());
            product.setCategory(productDto.getCategory());
            product.setDescription(productDto.getDescription());

            if (!imageFile.isEmpty()) {
                try {
                    String storageFileName = saveImageFile(imageFile);
                    product.setImageFileName(storageFileName);
                } catch (IOException e) {
                    System.out.println("Exception: " + e.getMessage());
                }
            }

            repo.save(product);
            return "redirect:/products";
        } else {
            System.out.println("Product not found with ID: " + id);
            return "redirect:/products";
        }
    }
    @GetMapping("/delete")
    public String deleteProduct(@RequestParam Long id) {
        try {
            Optional<Products> optionalProduct = repo.findById(id);
            if (optionalProduct.isPresent()) {
                Products product = optionalProduct.get();
                
                // Delete the product image
                Path imagePath = Paths.get("public/Image/" + product.getImageFileName());
                try {
                    if (Files.exists(imagePath)) {
                        Files.delete(imagePath);
                    }
                } catch (IOException ex) {
                    System.out.println("Exception deleting image file: " + ex.getMessage());
                }
    
                // Delete the product from the database
                repo.delete(product);
            } else {
                System.out.println("Product not found with ID: " + id);
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return "redirect:/products";
    }
    @GetMapping("/detail")
    public ModelAndView showProductDetail(@RequestParam Long id) {
        Optional<Products> optionalProduct = repo.findById(id);
        if (optionalProduct.isPresent()) {
            Products product = optionalProduct.get();
            ModelAndView mav = new ModelAndView("products/ProductDetail");
            mav.addObject("product", product);
            return mav;
        } else {
            System.out.println("Product not found with ID: " + id);
            return new ModelAndView("redirect:/products");
        }
    }
    @GetMapping({"", "/"})
    public String showProductList(
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "minPrice", required = false) Double minPrice,
        @RequestParam(value = "maxPrice", required = false) Double maxPrice,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        Model model) {
    
        Specification<Products> spec = Specification.where(null);
    
        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
        }
        
        if (minPrice != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        
        if (maxPrice != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
    
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Products> productsPage = repo.findAll(spec, pageable);
        
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("totalItems", productsPage.getTotalElements());
        model.addAttribute("size", size);
        
        return "products/index";
    }
    


}
    