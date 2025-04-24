package com.example._vs2Server.service;

import com.example._vs2Server.dto.BindingRequest;
import com.example._vs2Server.model.Binding;
import com.example._vs2Server.repository.BindingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BindingService {

    private final BindingRepository bindingRepository;

    public BindingService(BindingRepository bindingRepository) {
        this.bindingRepository = bindingRepository;
    }

    public void saveBindings(List<BindingRequest> requests) {
        requests.forEach(request -> {
            Binding binding = new Binding();

            // Source 1
            binding.setSource1League(request.getSource1().getLeagueName());
            binding.setSource1HomeTeam(request.getSource1().getHomeTeam());
            binding.setSource1AwayTeam(request.getSource1().getAwayTeam());

            // Source 2
            binding.setSource2League(request.getSource2().getLeagueName());
            binding.setSource2HomeTeam(request.getSource2().getHomeTeam());
            binding.setSource2AwayTeam(request.getSource2().getAwayTeam());

            // Source 3
            binding.setSource3League(request.getSource3().getLeagueName());
            binding.setSource3HomeTeam(request.getSource3().getHomeTeam());
            binding.setSource3AwayTeam(request.getSource3().getAwayTeam());

            bindingRepository.save(binding);
        });
    }

    public List<Binding> getAllBindings() {
        return bindingRepository.findAll();
    }
}