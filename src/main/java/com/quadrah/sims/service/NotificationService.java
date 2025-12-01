package com.quadrah.sims.service;

import com.quadrah.sims.model.MedicationInventory;
import com.quadrah.sims.model.StudentVisit;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void notifyEmergencyVisit(StudentVisit visit) {
        // Implementation for emergency notifications
        // Could be email, SMS, or internal alert system
        System.out.println("EMERGENCY: Visit #" + visit.getId() + " for student " +
                visit.getStudent().getFirstName() + " " + visit.getStudent().getLastName());
    }

    public void notifyDispositionChange(StudentVisit visit) {
        // Notify relevant parties about disposition change
        System.out.println("Disposition updated for Visit #" + visit.getId() + ": " + visit.getDisposition());
    }

    public void notifyLowStock(MedicationInventory medication) {
        // Notify about low stock levels
        System.out.println("LOW STOCK: " + medication.getMedicationName() +
                " (Current: " + medication.getCurrentStock() +
                ", Minimum: " + medication.getMinimumStock() + ")");
    }
}
