package org.mipams.jpegtrust.jpeg_systems;
import org.mipams.jumbf.entities.ServiceMetadata;
import org.mipams.jumbf.services.boxes.MemoryBoxService;
import org.springframework.stereotype.Service;

@Service
public class SaltHashBoxService extends MemoryBoxService<SaltHashBox> {
    
    ServiceMetadata serviceMetadata = new ServiceMetadata(SaltHashBox.TYPE_ID, SaltHashBox.TYPE);

    @Override
    protected SaltHashBox initializeBox() {
        return new SaltHashBox();
    }

    @Override
    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }
}



