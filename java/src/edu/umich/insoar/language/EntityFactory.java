package edu.umich.insoar.language;

import edu.umich.insoar.language.Patterns.*;

public class EntityFactory
{	
    public static LinguisticEntity createEntity(String entityType){
        if(entityType.equals(GoalInfo.TYPE)){
            return new GoalInfo();
        } else if(entityType.equals(LingObject.TYPE)){
            return new LingObject();
        } else if(entityType.equals(ObjectRelation.TYPE)){
            return new ObjectRelation();
        } else if(entityType.equals(ProposalInfo.TYPE)){
            return new ProposalInfo();
        } else if(entityType.equals(Sentence.TYPE)){
            return new Sentence();
        } else if(entityType.equals(VerbCommand.TYPE)){
            return new VerbCommand();
        } else if(entityType.equals(ObjectIdentification.TYPE)){
            return new ObjectIdentification();
        } else if(entityType.equals(BareAttributeResponse.TYPE)){
            return new BareAttributeResponse();
        } else if(entityType.equals(BareValueResponse.TYPE)){
            return new BareValueResponse();
        } else if(entityType.equals(RecognitionQuestion.TYPE)){
            return new RecognitionQuestion();
        } else if(entityType.equals(DescriptionRequest.TYPE)){
            return new DescriptionRequest();
        } else if(entityType.equals(PropertyRequest.TYPE)){
        	return new PropertyRequest();
        } else if(entityType.equals(CountRequest.TYPE)){
        	return new CountRequest();
        } else if(entityType.equals(SimpleCommand.TYPE)){
        	return new SimpleCommand();
        } else if(entityType.equals(ObjectState.TYPE)){
        	return new ObjectState();
        }else if(entityType.equals(RelationQuestion.TYPE)){
        	return new RelationQuestion();
        } else {
            return null;
        }
    }
}
