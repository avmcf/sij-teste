package git.testes;

//***************************************************************************************************
//Cleaner: Rotina de exclusão de publicações do diario oficial 	
//
//
//versao 1.0.0 	- 23 de Novembro de 2018
//				Versao Inicial
//
//versao 1.0.1 	- 17 de Dezembro de 2018
//				Correção na finalização da rotina e exclusão da pasta da edicao selecionada
//
//---------------------------------------------------------------------------------------------------------------------------
//
//V&C Consultoria Ltda.
//Autor: Arlindo Viana.
//
//***************************************************************************************************

import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.PropertyData;

import com.vconsulte.sij.base.InterfaceServidor;   

public class Teste extends JPanel implements ActionListener {
	
	public static String usuario = "sgj";
  public static String password = "934769386";

  public static String url = "http://192.168.1.30:8080";
//  public static String url = "http://127.0.0.1:8080";
  public static String baseFolder = "/Sites/advocacia/documentLibrary/Secretaria/Publicacoes";
	
	static InterfaceServidor conexao = new InterfaceServidor();
	
	static JFrame frame = new JFrame("Limpeza de Editais");
	static JPanel controlPane = new JPanel();

	static String idDoc = null;;	
	
	static List <String> folderIds = new ArrayList<String>();
	static String[] listaEdicoes = new String[50];
	
	static Session sessao;

	static String queryString = "";
	static String edicaoEscolhida = "";
	static String pastaEscolhida = "";
	
	static String token = "";

	static int k =0;
	static int opcao;
	static String a = null;
	
	static boolean escolheu = false;
	
	static Folder indexFolder;
	
	private static JTextField txt;
	private JButton btn1;
	
  static JTextArea output;
  static JList<String> list; 
  static JTable table;
  static String newline = "\n";
  static ListSelectionModel listSelectionModel;
  static String[] listData = new String[7];
  
  private JTextField entry;
  
  public Clean() {
  	super(new BorderLayout());
  	
  	btn1=new JButton("Limpar");
  	btn1.addActionListener(this);
  	txt=new JTextField(25);    	
      list = new JList<String>(listData);

      listSelectionModel = list.getSelectionModel();
      listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());

      JScrollPane listPane = new JScrollPane(list);
      
      listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      //Build output area.
      output = new JTextArea(1, 10);
      output.setEditable(false);
      JScrollPane outputPane = new JScrollPane(output,
              ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      
      //Do the layout.
      JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      add(splitPane, BorderLayout.CENTER);

      JPanel topHalf = new JPanel();
      topHalf.setLayout(new BoxLayout(topHalf, BoxLayout.PAGE_AXIS));
      			
		// este é o list_view
      JPanel listContainer = new JPanel(new GridLayout(1,1));			// container da list_view
      listContainer.setBorder(BorderFactory.createTitledBorder("Edições localizadas"));
      listContainer.add(listPane);									// listPane é a lista de edicoes
      topHalf.add(listContainer);										// inclusão da list_view no container

      topHalf.add(txt);
      
      btn1.setAlignmentY(CENTER_ALIGNMENT);
      topHalf.add(btn1);
      
      splitPane.add(topHalf);
      
      JPanel bottomHalf = new JPanel(new BorderLayout());
      bottomHalf.add(controlPane, BorderLayout.PAGE_START);
      bottomHalf.add(outputPane, BorderLayout.CENTER);
      bottomHalf.setPreferredSize(new Dimension(450, 135));
      splitPane.add(bottomHalf);
  }
  
  public static void main(String[] args){   
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
      		if (!conectaServidor()) {
          		JOptionPane.showMessageDialog(null, "Erro na conexão com o servidor");
      			finalizaProcesso();
      		}
				carregaInformacoes();				
          	apresentaJanela();
          }
      });
  }

  public static void carregaInformacoes() {	

		Map<String, String> mapEdicoes = new HashMap<String, String>();
		mapEdicoes = conexao.listarEdicoes(sessao, "/Sites/advocacia/documentLibrary/Secretaria/Carregamento");
		carregaEdicoes(mapEdicoes);
  }

	public static boolean conectaServidor() {

		conexao.setUser(usuario);
		conexao.setPassword(password);
		conexao.setUrl(url);
		sessao = InterfaceServidor.serverConnect();
		if (sessao == null) {
			JOptionPane.showMessageDialog(null, "Erro na conexão com o servidor");
			finalizaProcesso();
			return false;
		}
		return true;
	}
  
	public static String obtemHrAtual() {	
		String hr = "";
		String mn = "";
		String sg = "";
		Calendar data = Calendar.getInstance();
		hr = Integer.toString(data.get(Calendar.HOUR_OF_DAY));
		mn = Integer.toString(data.get(Calendar.MINUTE));
		sg = Integer.toString(data.get(Calendar.SECOND));	
		return completaEsquerda(hr,'0',2)+":"+completaEsquerda(mn,'0',2)+":"+completaEsquerda(sg, '0', 2);
	}
	
	public static String completaEsquerda(String value, char c, int size) {
		String result = value;
		while (result.length() < size) {
			result = c + result;
		}
		return result;
	}
	
	public static void finalizaProcesso() {
		JOptionPane.showMessageDialog(null, "Fim do Processamento");
      System.exit(0);
	}
	
  class SharedListSelectionHandler implements ListSelectionListener {
      public void valueChanged(ListSelectionEvent e) { 
          ListSelectionModel lsm = (ListSelectionModel)e.getSource();            
          opcao = e.getFirstIndex();;
          edicaoEscolhida = listData[opcao];
          if(!escolheu) {
          	txtMensagem("Edicao escolhida: " + edicaoEscolhida);
          	escolheu = true;
          }          
      } 
  }
     
  public static void msgMensagem(String linha) {
  	output.append(linha);
  }
  
  public static void txtMensagem(String mensagem) {
  	txt.setText(mensagem);
  }
  
  public static void carregaEdicoes(Map<String, ?> edicoes) {   	
  	int ix = 0;
  	Set<String> chaves = edicoes.keySet();
		for (Iterator<String> iterator = chaves.iterator(); iterator.hasNext();){
			String chave = iterator.next();
			if(chave != null) {
				listData[ix] = (edicoes.get(chave).toString());
				folderIds.add(chave);
				ix++;
			}
		}
  }
  
  public static void apresentaJanela() {       
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      //Create and set up the content pane.
      Clean demo = new Clean();
      demo.setOpaque(true);
      frame.setContentPane(demo);

      //Display the window.
      frame.pack();
      frame.setVisible(true);
      txtMensagem("Escolha uma edicao.");
  }
  
  public void actionPerformed(ActionEvent evt) {
		Object obj=evt.getSource();
		try {
			limpar();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
  
  public static void limpar() throws Exception {

  	List <String> pastaExclir = new ArrayList<String>();
  	int qtd = 1;
  	
		msgMensagem(obtemHrAtual() +" - Início da exclusão");
		txt.setText(edicaoEscolhida);	
		pastaExclir = conexao.getFolderInfo(sessao, folderIds.get(opcao));		
		String pastaId = pastaExclir.get(0);

		for (int x=0; x<=40000; x++) {

			queryString = "select cmis:objectId from sij:edital where in_folder('" + pastaId + "')";		
			ItemIterable<QueryResult> results = sessao.query(queryString, false);
			
			// delete each result    	
	   		if (results.getTotalNumItems() > 0) { 
	   			msgMensagem(obtemHrAtual() + " - Encontrados " + results.getTotalNumItems() + " editais para excluir.");
	   		}
	    	for (QueryResult qResult : results) {
	     	   PropertyData<?> propData = qResult.getPropertyById("cmis:objectId");
	     	   String objectId = (String) propData.getFirstValue();
	     	   InterfaceServidor.excluiEdital(sessao, objectId);
	     	   msgMensagem(obtemHrAtual() + " - " + qtd++ + " - excluido: " + objectId);	     	   
	     	}
	    	if(results.getTotalNumItems() == 0) {
	    		InterfaceServidor.excluiPasta(sessao, pastaId);
	    		escolheu = false;
	    		break;
	    	}
		}
  	finalizaProcesso();
  }
}