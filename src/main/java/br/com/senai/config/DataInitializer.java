package br.com.senai.config;

import br.com.senai.model.DTO.DocumentDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.entity.CategoryEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.AuthService;
import br.com.senai.service.SupabaseAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final SupabaseAuthService supabaseAuthService;
    private final AuthService authService;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            if (userRepository.count() == 0) {
                try {
                    System.out.println("🚀 Iniciando inicialização de dados...");

                    // 1. Criar usuário primeiro
                    UserEntity user = createDefaultUser();

                    // 2. AGORA criar serviços - dentro do mesmo contexto
                    createDefaultServices(user);

                    System.out.println("✅ Inicialização concluída com sucesso!");

                } catch (Exception e) {
                    System.err.println("❌ Erro durante inicialização: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("⏭️ Banco já possui dados. Inicialização ignorada.");
            }
        };
    }

    private UserEntity createDefaultUser() throws Exception {
        String plainPassword = "123123";

        UserDTO defaultUser = new UserDTO();
        defaultUser.setName("Bertrania Dude");
        defaultUser.setEmail("email@email.com");
        defaultUser.setPhoneNumber(5547912345678L);
        defaultUser.setPassword(plainPassword);

        // Documento
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setName("documento_padrao.png");
        documentDTO.setType("image/png");

        byte[] imageBytes = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("images.png")).readAllBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        documentDTO.setData(base64Image);
        defaultUser.setDocument(documentDTO);

        // Registrar no Supabase
        Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("name", defaultUser.getName());
        userMetadata.put("phone", defaultUser.getPhoneNumber());

        var supabaseUserDTO = supabaseAuthService.signUp(
                defaultUser.getEmail(),
                defaultUser.getPassword(),
                userMetadata
        );

        // Registrar no banco local
        UserEntity createdUser = authService.register(defaultUser, supabaseUserDTO.getId());
        System.out.println("✅ Usuário criado: " + createdUser.getEmail());

        return createdUser;
    }

    private void createDefaultServices(UserEntity userCreator) throws Exception {
        System.out.println("🛠️ Criando serviços padrão...");

        // Garantir que temos a entidade mais recente do banco
        UserEntity managedUser = userRepository.findById(userCreator.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado após criação"));

        // Carregar imagem uma única vez
        byte[] serviceImageBytes = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("images.png")).readAllBytes();
        String base64ServiceImage = "data:image/png;base64," + Base64.getEncoder().encodeToString(serviceImageBytes);

        // Lista de serviços para criar
        List<ServiceEntity> servicesToCreate = Arrays.asList(
                createService("Manutenção Preventiva de Eletrodomésticos",
                        "Realizamos manutenção preventiva em geladeiras, lavadoras, micro-ondas e outros eletrodomésticos. Inclui limpeza, lubrificação e ajustes necessários para prolongar a vida útil do equipamento.",
                        6, "PRESENCIAL", LocalDate.now().plusDays(15),
                        createCategoryList("Manutenção"), base64ServiceImage, managedUser),

                createService("Desentupimento de Pia e Vaso Sanitário",
                        "Serviço de desentupimento rápido e eficaz para pias, ralos, vasos sanitários e tubulações. Utilizamos equipamentos modernos sem danificar a estrutura do local.",
                        4, "PRESENCIAL", LocalDate.now().plusDays(7),
                        createCategoryList("Encanamento"), base64ServiceImage, managedUser),

                createService("Instalação de Tomadas e Interruptores",
                        "Instalação elétrica residencial e comercial. Inclui troca de interruptores, tomadas, quadro de luz e adequação à norma técnica. Garantia de segurança e qualidade.",
                        8, "PRESENCIAL", LocalDate.now().plusDays(10),
                        createCategoryList("Elétrica"), base64ServiceImage, managedUser)
        );

        // Salvar todos os serviços
        serviceRepository.saveAll(servicesToCreate);
        System.out.println("✅ " + servicesToCreate.size() + " serviços criados com sucesso!");
    }

    private ServiceEntity createService(String title, String description, int timeChronos,
                                        String modality, LocalDate deadline,
                                        List<CategoryEntity> categories, String base64Image,
                                        UserEntity userCreator) {
        ServiceEntity service = new ServiceEntity();
        service.setTitle(title);
        service.setDescription(description);
        service.setTimeChronos(timeChronos);
        service.setModality(modality);
        service.setDeadline(deadline);
        service.setPostedAt(LocalDateTime.now());
        service.setCategoryEntities(categories);
        service.setUserCreator(userCreator);

        // Processar imagem
        String imageData = base64Image.contains(",")
                ? base64Image.substring(base64Image.indexOf(",") + 1)
                : base64Image;
        service.setServiceImage(Base64.getDecoder().decode(imageData));

        return service;
    }

    private List<CategoryEntity> createCategoryList(String... categoryNames) {
        List<CategoryEntity> categories = new ArrayList<>();
        for (String name : categoryNames) {
            CategoryEntity category = new CategoryEntity();
            category.setName(name);
            categories.add(category);
        }
        return categories;
    }
}