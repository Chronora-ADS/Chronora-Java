package br.com.senai.rabbitMQ;

import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@EnableScheduling
public class DeadlineSchedulerService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private NotificationProducer notificationProducer;

    // Executa a cada minuto
    @Scheduled(fixedDelay = 60000L)
    public void checkExpiredDeadlines() {
        LocalDate today = LocalDate.now();
        List<ServiceEntity> expiredServices = serviceRepository.findByDeadlineBeforeAndDeadlineNotifiedFalse(today);

        for (ServiceEntity service : expiredServices) {
            notificationProducer.sendNotification(service.getUserCreator().getId(), service.getId());
            service.setDeadlineNotified(true);
            serviceRepository.save(service);
        }
    }
}