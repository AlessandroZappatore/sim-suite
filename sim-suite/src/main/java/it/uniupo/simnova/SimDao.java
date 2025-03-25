package it.uniupo.simnova;

import it.uniupo.simnova.simcreation.Scenario;
import it.uniupo.simnova.utils.DBConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

public class SimDao {

    public List<Scenario> getAllScenarios(){
        final String sql = "SELECT * FROM scenario";

        List<Scenario> scenarios = new LinkedList<>();

        try{
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement st = conn.prepareStatement(sql);

            ResultSet rs = st.executeQuery();

            while(rs.next()){
                Scenario s = new Scenario(rs.getInt("id"), rs.getString("titolo"), rs.getString("nome_paziente"), rs.getString("patologia"), rs.getString("descrizione"), rs.getString("patto_aula"), rs.getString("azione_chiave"), rs.getString("obiettivo"), rs.getString("materiale"), rs.getString("moulage"), rs.getString("liquidi"), rs.getString("liquidi"), rs.getFloat("timer_generale") ,null, null);
                scenarios.add(s);
            }
            st.close();
            conn.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return scenarios;
    }

}
