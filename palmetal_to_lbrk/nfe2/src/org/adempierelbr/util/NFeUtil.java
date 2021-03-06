/******************************************************************************
 * Product: ADempiereLBR - ADempiere Localization Brazil                      *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.adempierelbr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.adempiere.model.POWrapper;
import org.adempierelbr.model.MLBRNotaFiscal;
import org.adempierelbr.nfe.beans.InutilizacaoNF;
import org.adempierelbr.wrapper.I_W_C_City;
//import org.adempierelbr.wrapper.I_W_C_City;
import org.compiere.model.MAttachment;
import org.compiere.model.MAttachmentEntry;
import org.compiere.model.MCity;
import org.compiere.model.MOrgInfo;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.utils.DigestOfFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.inf.portalfiscal.www.nfe.wsdl.cadconsultacadastro2.CadConsultaCadastro2Stub;
import br.inf.portalfiscal.www.nfe.wsdl.nfecancelamento2.NfeCancelamento2Stub;
import br.inf.portalfiscal.www.nfe.wsdl.nfeinutilizacao2.NfeInutilizacao2Stub;
import br.inf.portalfiscal.www.nfe.wsdl.nferecepcao2.NfeRecepcao2Stub;
import br.inf.portalfiscal.www.nfe.wsdl.nferetrecepcao2.NfeRetRecepcao2Stub;
import br.inf.portalfiscal.www.nfe.wsdl.nfestatusservico2.NfeStatusServico2Stub;
import br.inf.portalfiscal.www.nfe.wsdl.recepcaoevento.RecepcaoEventoStub;

import com.thoughtworks.xstream.XStream;

/**
 * 	Utilitários para gerar a NFe.
 *
 * @author Ricardo Santana
 */
public abstract class NFeUtil
{

	/**	Logger				*/
	private static CLogger log = CLogger.getCLogger(NFeUtil.class);

	/** Versão				*/
	public static final String VERSAO			= "2.00";
	public static final String VERSAO_APP		= "2.00";
	public static final String VERSAO_CCE		= "1.00";

	/** XML					*/
	private static String FILE_EXT      = "-dst.xml";
	public static final long XML_SIZE   = 500;

	/** Status				*/
	public static final String AUTORIZADA = "100";
	public static final String CANCELADA  = "101";

	/** Eventos				*/
	public static final String NFE_EVENTO_CCE 		= "110110";
	public static final String NFE_EVENTO_CANCEL 	= "110111";
	
	/** Reference NFeStatus */
	public static final int NFeReference   = 1100004;

	/**
	 * Gera o cabeçalho da NFe
	 *
	 * @return cabecalho
	 */
	public static String geraCabecNFe(){
		String cabecalho = "<NFe xmlns=\"http://www.portalfiscal.inf.br/nfe\">";
		return cabecalho;
	} //geraCabecNFe

	/**
	 * Gera o rodapé da NFe
	 *
	 * return rodape
	 */
	public static String geraRodapNFe(){
		String rodape = "</NFe>";
		return rodape;
	} //geraRodapNFe

	/**
	 * Gera o cabeçalho distribuição
	 *
	 * @return Cabeçalho distribuiçãi
	 */
	public static String geraCabecDistribuicao(){
		String cabecalho = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
		 "<nfeProc xmlns=\"http://www.portalfiscal.inf.br/nfe\"  versao=\"" + VERSAO + "\">";

		return cabecalho;
	}
	
	/**
	 * Método para gerar cabeçalho envio do lote NF 2.00
	 * @param region
	 * @return NfeRecepcao2Stub.NfeCabecMsgE
	 */
	public static RecepcaoEventoStub.NfeCabecMsgE geraCabecEvento (String region)
	{
		RecepcaoEventoStub.NfeCabecMsg cabecMsg = new RecepcaoEventoStub.NfeCabecMsg();
		cabecMsg.setCUF(region);
		cabecMsg.setVersaoDados(VERSAO_CCE);

		RecepcaoEventoStub.NfeCabecMsgE cabecMsgE = new RecepcaoEventoStub.NfeCabecMsgE();
		cabecMsgE.setNfeCabecMsg(cabecMsg);

		return cabecMsgE;
	}	//	geraCabecEvento

	/**
	 * Rodapé padrão Distribuição
	 *
	 * @param chNFe
	 * @param nProt
	 * @param tpAmb
	 * @param dhRecbto
	 * @param digVal
	 * @param cStat
	 * @param xMotivo
	 * @return XML
	 */
	public static String geraRodapDistribuicao (String chNFe, String nProt, String tpAmb, String dhRecbto,
			                                    String digVal, String cStat, String xMotivo)
	{
		String dados = 	"<protNFe xmlns=\"http://www.portalfiscal.inf.br/nfe\" versao=\"" + VERSAO + "\"><infProt>" +
				        "<tpAmb>"+tpAmb+"</tpAmb>" +
				        "<verAplic>"+VERSAO_APP+"</verAplic>" +
				        "<chNFe>"+chNFe+"</chNFe>" +
				        "<dhRecbto>"+dhRecbto+"</dhRecbto>" +
				        "<nProt>"+nProt+"</nProt>" +
				        "<digVal>"+digVal+"</digVal>" +
				        "<cStat>"+cStat+"</cStat>" +
				        "<xMotivo>"+xMotivo+"</xMotivo></infProt></protNFe></nfeProc>";

		return dados;
	}	//	RodapDistribuicao

	/**
	 * Método para gerar cabeçalho status serviço NF 2.00
	 * @param region
	 * @return NfeStatusServico2Stub.NfeCabecMsgE
	 */
	public static NfeStatusServico2Stub.NfeCabecMsgE geraCabecStatusServico(String region){

		NfeStatusServico2Stub.NfeCabecMsg cabecMsg = new NfeStatusServico2Stub.NfeCabecMsg();
		cabecMsg.setCUF(region);
		cabecMsg.setVersaoDados(VERSAO);

		NfeStatusServico2Stub.NfeCabecMsgE cabecMsgE = new NfeStatusServico2Stub.NfeCabecMsgE();
		cabecMsgE.setNfeCabecMsg(cabecMsg);

		return cabecMsgE;
	} //geraCabecStatusServico

	/**
	 * Método para gerar dados para consulta status serviço NF 2.00
	 * @param envType
	 * @param region
	 * @return msg
	 */
	public static String geraMsgStatusServico(String envType, String region){

		String msg =
			"<nfeDadosMsg>" +
		    	"<consStatServ versao=\"" + VERSAO + "\" xmlns=\"http://www.portalfiscal.inf.br/nfe\">" +
		        	"<tpAmb>"+envType+"</tpAmb>" +
		        	"<cUF>"+region+"</cUF>" +
		        	"<xServ>STATUS</xServ>" +
		        "</consStatServ>" +
		    "</nfeDadosMsg>";

		return msg;
	} //geraMsgStatusServico

	/**
	 * Método para gerar cabeçalho consulta cadastro NF 2.00
	 * @param region
	 * @return CadConsultaCadastro2Stub.NfeCabecMsgE
	 */
	public static CadConsultaCadastro2Stub.NfeCabecMsgE geraCabecConsultaCadastro(String region){

		CadConsultaCadastro2Stub.NfeCabecMsg cabecMsg = new CadConsultaCadastro2Stub.NfeCabecMsg();
		cabecMsg.setCUF(region);
		cabecMsg.setVersaoDados(VERSAO);

		CadConsultaCadastro2Stub.NfeCabecMsgE cabecMsgE = new CadConsultaCadastro2Stub.NfeCabecMsgE();
		cabecMsgE.setNfeCabecMsg(cabecMsg);

		return cabecMsgE;
	} //geraCabecConsultaCadastro

	/**
	 * Método para gerar dados para consulta cadastro NF 2.00
	 * @param envType
	 * @param region
	 * @return msg
	 */
	public static String geraMsgConsultaCadastro(String region, String IE, String CNPJ){

		String arg = "";

		if (CNPJ != null && CNPJ.length() == 18){ //CNPJ
			//if (IE != null && !IE.equalsIgnoreCase("ISENTO")){ //BUG NO WEBSERVICE -- NAO RETORNA CONSULTA DE IE CORRETA
			//	arg = "<IE>"+TextUtil.toNumeric(IE)+"</IE>";
			//}
			//else{
				arg = "<CNPJ>"+TextUtil.toNumeric(CNPJ)+"</CNPJ>";
			//}
		}
		else{ //CPF
			arg = "<CPF>"+TextUtil.toNumeric(CNPJ)+"</CPF>";
		}

		String msg =
			"<nfeDadosMsg>" +
				"<ConsCad xmlns=\"http://www.portalfiscal.inf.br/nfe\" versao=\"2.00\">" +
					"<infCons>" +
		        		"<xServ>CONS-CAD</xServ>" +
		        		"<UF>"+region+"</UF>" +
		        		arg +
		    		"</infCons>" +
		        "</ConsCad>" +
		    "</nfeDadosMsg>";

		return msg;
	} //geraMsgConsultaCadastro


	/**
	 * Gera o cabeçalho do lote
	 *
	 * @return Cabeçalho do lote
	 */
	public static String geraCabecLoteNFe (String lote){
		String cabecalho = "<enviNFe xmlns=\"http://www.portalfiscal.inf.br/nfe\" versao=\"2.00\">" +
	   "<idLote>"+lote+"</idLote>";

		return cabecalho;
	} // geraCabecLoteNFe

	/**
	 * Método para gerar cabeçalho envio do lote NF 2.00
	 * @param region
	 * @return NfeRecepcao2Stub.NfeCabecMsgE
	 */
	public static NfeRecepcao2Stub.NfeCabecMsgE geraCabecRecepcao(String region){

		NfeRecepcao2Stub.NfeCabecMsg cabecMsg = new NfeRecepcao2Stub.NfeCabecMsg();
		cabecMsg.setCUF(region);
		cabecMsg.setVersaoDados(VERSAO);

		NfeRecepcao2Stub.NfeCabecMsgE cabecMsgE = new NfeRecepcao2Stub.NfeCabecMsgE();
		cabecMsgE.setNfeCabecMsg(cabecMsg);

		return cabecMsgE;
	} //geraCabecRecepcao

	/**
	 * Método para gerar cabeçalho consulta lote NF 2.00
	 * @param region
	 * @return NfeRetRecepcao2Stub.NfeCabecMsgE
	 */
	public static NfeRetRecepcao2Stub.NfeCabecMsgE geraCabecRetRecepcao(String region){

		NfeRetRecepcao2Stub.NfeCabecMsg cabecMsg = new NfeRetRecepcao2Stub.NfeCabecMsg();
		cabecMsg.setCUF(region);
		cabecMsg.setVersaoDados(VERSAO);

		NfeRetRecepcao2Stub.NfeCabecMsgE cabecMsgE = new NfeRetRecepcao2Stub.NfeCabecMsgE();
		cabecMsgE.setNfeCabecMsg(cabecMsg);

		return cabecMsgE;
	} //geraCabecRetRecepcao

	/**
	 * Método para gerar dados para consulta do lote NF 2.00
	 * @param envType
	 * @param region
	 * @return msg
	 */
	public static String geraMsgRetRecepcao (String recibo, String envType)
	{
		String msg = 	"<consReciNFe xmlns=\"http://www.portalfiscal.inf.br/nfe\" versao=\"2.00\">" +
							"<tpAmb>"+envType+"</tpAmb>" +
						"<nRec>"+recibo+"</nRec>"+
						"</consReciNFe>";
		return msg;
	}	//geraMsgRetRecepcao

	/**
	 * Método para gerar cabeçalho cancelamento NF 2.00
	 * @param region
	 * @return NfeCancelamento2Stub.NfeCabecMsgE
	 */
	public static NfeCancelamento2Stub.NfeCabecMsgE geraCabecCancelamento(String region){

		NfeCancelamento2Stub.NfeCabecMsg cabecMsg = new NfeCancelamento2Stub.NfeCabecMsg();
		cabecMsg.setCUF(region);
		cabecMsg.setVersaoDados(VERSAO);

		NfeCancelamento2Stub.NfeCabecMsgE cabecMsgE = new NfeCancelamento2Stub.NfeCabecMsgE();
		cabecMsgE.setNfeCabecMsg(cabecMsg);

		return cabecMsgE;
	} //geraCabecCancelamento

	/**
	 * Método para gerar dados para cancelamento NF 2.00
	 * @param Chave da NF-e
	 * @param Protocolo de Autorização
	 * @param Tipo de Ambiente
	 * @param Motivo
	 * @return	XML
	 */
	public static String geraMsgCancelamento (String chNFe, String protocolNFe, String envType, String motivo)
	{
		String msg = "<cancNFe xmlns=\"http://www.portalfiscal.inf.br/nfe\" versao=\"2.00\">" +
							  "<infCanc Id=\"ID"+chNFe+"\">"+
						          "<tpAmb>"+envType+"</tpAmb>"+
						          "<xServ>CANCELAR</xServ>"+
						          "<chNFe>"+chNFe+"</chNFe>"+
						          "<nProt>"+protocolNFe+"</nProt>"+
						          "<xJust>"+motivo+"</xJust>"+
					          "</infCanc>"+
					  "</cancNFe>";
		return msg;
	}	// geraMsgCancelamento

	public static File generateDistribution(MLBRNotaFiscal nf) throws Exception{

		File attach = null;

		if (nf.getlbr_NFeProt() == null || nf.getlbr_NFeProt().equals("")) //Verifica se foi processada
			return attach;

		String status = nf.getlbr_NFeStatus();

		if (status.equals(AUTORIZADA)){ //Autorizado o uso da NF-e
			FILE_EXT = "-dst.xml";
		}
		else if (status.equals(CANCELADA)){ //Cancelamento de NF-e homologado
			FILE_EXT = "-can.xml";
		}

		File xml = getAttachmentEntryFile(nf.getAttachment().getEntry(0));
		if (xml == null || xml.getName().endsWith(FILE_EXT)) //Já está no padrão de distribuição
			return attach;

	    String dados = XMLtoString(xml);
	    //
	    String cabecalho  = geraCabecDistribuicao();
		//
	    String rodape 	  = geraRodapDistribuicao(nf.getlbr_NFeID(), nf.getlbr_NFeProt(), getEnvType(nf.getCtx()),
	        		                                  timeToString(nf.getDateTrx()), nf.getlbr_DigestValue(),
	        		                                  status,getNFeStatus(status));
		//
		String dadosEmXML = cabecalho + dados + rodape;
		attach = new File(TextUtil.generateTmpFile(dadosEmXML, nf.getlbr_NFeID() + FILE_EXT));

		nf.getAttachment(true).delete(true); //Exclui anexo anterior. BUG ADempiere

		return attach;
	} //NFeDistribution

	/**
	 * update Attachment
	 * @param nf
	 * @param xml
	 * @return true = success, false = error
	 */
	public static boolean updateAttach(MLBRNotaFiscal nf, File xml){

		if (xml != null){
			MAttachment attachDist = nf.createAttachment(true);
			attachDist.addEntry(xml);
			return attachDist.save();
		}

		return true;
	}

	public static String XMLtoString(File xml) throws Exception{

		String dados = "";
		if (xml == null)
			return dados;

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        InputStream inputStream = new FileInputStream(xml);
        org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
        StringWriter stw = new StringWriter();
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.transform(new DOMSource(doc), new StreamResult(stw));
        dados = stw.toString();

        if (dados.indexOf("<NFe") != -1){
	        dados = dados.substring(dados.indexOf("<NFe"),dados.indexOf("</NFe>")+6);
	        if (dados.startsWith("<NFe>")){
	        	dados = geraCabecNFe() + dados.substring(5);
	        }
        }

        return dados;
	} //XMLtoString
	
	/**
	 * removeIndent
	 * @param xml
	 * @return
	 */
	public static String removeIndent(String xml){
		
		StringBuilder newXML = new StringBuilder("");
		
		int i = 0;
		while (i < xml.length()){
			int end = xml.indexOf(">", i);
			if (end != -1){
				newXML.append(xml.substring(i, end+1));
				i = end + 1;
				
				int newTag = xml.indexOf("<", i);
				int endTag = xml.indexOf("</", i);
				
				if (endTag > newTag){
					i = newTag;
				}
				
				//check if value is blank
				if (endTag != -1){
					String value = xml.substring(i, endTag);
					if (value.trim().isEmpty())
						i = endTag;
				}
				
			}
		}
					
		return newXML.toString();
	} //removeIndent
	
	
	/**
	 * Get Attachment
	 *
	 * @param entry
	 * @return
	 */
	public static File getAttachmentEntryFile(MAttachmentEntry entry)
	{
		//BF - Attachment on FileSystem
		String fileName = entry.toString();
		if(fileName!=null && fileName.lastIndexOf(File.separator) != -1)
			fileName = fileName.substring(fileName.lastIndexOf(File.separator)+1);
		
		String localFile = System.getProperty("java.io.tmpdir")
				+ System.getProperty("file.separator") + fileName;
		String downloadedLocalFile = System.getProperty("java.io.tmpdir")
				+ System.getProperty("file.separator") + "TMP" + fileName;
		File attachedFile = new File(localFile);
		if (attachedFile.exists())
		{
			String localMD5hash = DigestOfFile.GetLocalMD5Hash(attachedFile);
			String entryMD5hash = DigestOfFile.getMD5Hash(entry.getData());
			if (localMD5hash.equals(entryMD5hash))
			{
				log.fine("no need to download: local file is up-to-date");
			}
			else
			{
				log.fine("file attached is different that local one, download and replace");
				File downloadedFile = new File(downloadedLocalFile);
				entry.getFile(downloadedFile);
				attachedFile.delete();
				downloadedFile.renameTo(attachedFile);
			}
		}
		else
		{
			entry.getFile(attachedFile);
		}
		return attachedFile;
	}	//	getAttachmentEntryFile

	/**
	 * String para Timestamp
	 * @param dhRecbto
	 * @return
	 */
	public static Timestamp stringToTime(String dhRecbto){
		return TextUtil.stringToTime(dhRecbto.replace('T', ' '), "yyyy-MM-dd HH:mm:ss");
	} //StringToDate

	/**
	 * Timestamp para String
	 * @param dhRecbto
	 * @return
	 */
	public static String timeToString(Timestamp dhRecbto){
		return TextUtil.timeToString(dhRecbto, "yyyy-MM-dd HH:mm:ss").replace(' ', 'T');
	} //DateToString

	/**
	 * getEnvType
	 * @param ctx
	 * @return envType
	 */
	public static String getEnvType(Properties ctx){
		MOrgInfo oi = MOrgInfo.get(ctx, Env.getAD_Org_ID(ctx),null);
		String envType 	= oi.get_ValueAsString("lbr_NFeEnv");

		if (envType == null || envType.equals(""))
			envType = "2"; //Homologação

		return envType;
	} //getEnvType

	public static String getNFeStatus(String status)
	{

		String sql = "SELECT Name FROM AD_Ref_List " +
				     "WHERE AD_Reference_ID = ? AND Value = ?";

		String NFeStatus = DB.getSQLValueString(null, sql,
				new Object[]{NFeReference, status});

		return NFeStatus;
	}	//getNFeStatus

	/**
	 * Valida tamanho do Arquivo XML
	 * @param file
	 * @return error or null
	 */
	public static String validateSize(File file){

		long size = file.length(); //bytes
		if ((size/1024) > XML_SIZE){ //check kbytes
			String erro = "Tamanho do Arquivo XML inválido > " + XML_SIZE + " kbytes";
			log.severe(erro);
			return erro;
		}

		return null;
	} //validateSize
	
	/**
	 * 		Verifica a NF e retorna o ID da mesma
	 * 
	 * 	@param nf a ser verificada
	 * 	@return	ID da NF Eletrônica ou NULL quando houver erro
	 */
	public static String checkXMLFile (MLBRNotaFiscal nf)
	{
		//	Validação preliminar
		if (nf == null || nf.getAttachment() == null || nf.getAttachment().getEntry(0) == null)
			return null;
		
		String nfeID = nf.getAttachment().getEntry(0).toString();
		//
		if(nfeID != null)
		{
			int beginIndex = nfeID.lastIndexOf(File.separator)+1;
			int endIndex   = nfeID.lastIndexOf('-');
			
			nfeID = nfeID.substring(beginIndex, endIndex == -1 ? nfeID.length() : endIndex);
		}
		
		return nfeID;
	}	//	checkXMLFile


	/**
	 * 	Get Value from XML
	 *
	 * @param node
	 * @param Tag
	 * @return
	 */
	public static String getValue (Node node, String Tag)
	{
		if (node == null)
			return "";

		NodeList nl = ((Element) node).getElementsByTagName(Tag);
		if (nl == null)
			return "";

		Element el = (Element) nl.item(0);
		if (el == null)
			return "";

		nl = el.getChildNodes();
		if (nl == null)
			return "";

		return nl.item(0).getNodeValue();
	}	//	getValue

	/**
	 * 	Get Value from XML
	 *
	 * @param node
	 * @param Tag
	 * @return
	 */
	public static String getValue (Document doc, String Tag)
	{
		if (doc.getElementsByTagName(Tag) == null)
			return "";

		if (doc.getElementsByTagName(Tag).item(0) == null)
			return "";

		return doc.getElementsByTagName(Tag).item(0).getTextContent();
	}	//	getValue

	/**
	 * 	Get Resource to include in XSD File
	 *
	 * @param clazz
	 * @param name
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static URL getResource(Class clazz, String name)
	{
        // Get the URL for the resource using the standard behavior
        URL result = clazz.getResource(name);

        // Check to see that the URL is not null and that it's a JAR URL.
        if (result != null && "jar".equalsIgnoreCase(result.getProtocol())) {
            // Get the URL to the "clazz" itself.  In a JNLP environment, the "getProtectionDomain" call should succeed only with properly signed JARs.
            URL classSourceLocationURL = clazz.getProtectionDomain().getCodeSource().getLocation();
            // Create a String which embeds the classSourceLocationURL in a JAR URL referencing the desired resource.
            String urlString = MessageFormat.format("jar:{0}!/{1}/{2}", classSourceLocationURL.toExternalForm(), packageNameOfClass(clazz).replaceAll("\\.", "/"), name);

            // Check to see that new URL differs.  There's no reason to instantiate a new URL if the external forms are identical (as happens on pre-1.5.0_16 builds of the JDK).
            if (urlString.equals(result.toExternalForm()) == false) {
                // The URLs are different, try instantiating the new URL.
                try {
                    result = new URL(urlString);
                } catch (MalformedURLException malformedURLException) {
                    throw new RuntimeException(malformedURLException);
                }
            }
        }
        return result;
    }

	/**
	 * 	Cabeçalho da Inutilização
	 * @param region
	 * @return
	 */
	public static NfeInutilizacao2Stub.NfeCabecMsgE geraCabecInutilizacao (String region)
	{
		NfeInutilizacao2Stub.NfeCabecMsg cabecMsg = new NfeInutilizacao2Stub.NfeCabecMsg();
		cabecMsg.setCUF(region);
		cabecMsg.setVersaoDados(VERSAO);

		NfeInutilizacao2Stub.NfeCabecMsgE cabecMsgE = new NfeInutilizacao2Stub.NfeCabecMsgE();
		cabecMsgE.setNfeCabecMsg(cabecMsg);

		return cabecMsgE;
	}	//	geraCabecInutilizacao
	
	/**
	 * 	Gera o XML para inutilização da NF
	 * 
	 * 	@return cabeçalho de envio
	 */
	public static String geraInutilizacao (InutilizacaoNF nf)
	{
		XStream xml = new XStream();
		//
		xml.alias("infInut", InutilizacaoNF.class);
		xml.useAttributeFor(InutilizacaoNF.class, "Id");
		xml.omitField(InutilizacaoNF.class, "msg");
		xml.omitField(InutilizacaoNF.class, "log");
		// 
		StringBuffer inut = new StringBuffer("");
		//
		inut.append("<inutNFe xmlns=\"http://www.portalfiscal.inf.br/nfe\" versao=\"2.00\">");
		inut.append(xml.toXML(nf));
		inut.append("</inutNFe>");
		//
		return inut.toString();
	}	//	geraInutilizacao

	/**
	 * 	packageNameOfClass
	 *
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String packageNameOfClass(Class clazz)
    {
        String result = "";
        String className = clazz.getName();
        int lastPeriod = className.lastIndexOf(".");

        if (lastPeriod > -1) {
            result = className.substring(0, lastPeriod);
        }
        return result;
    }	//	packageNameOfClass
	
	/**
	 * 	Pega o código da cidade
	 * 	
	 * 	@param oi
	 * 	@return	region code
	 */
	public static String getCityCode (MOrgInfo oi)
	{
		if (oi == null || oi.getC_Location_ID() == 0 || oi.getC_Location().getC_City_ID() == 0)
			return null;
		//
		I_W_C_City city = POWrapper.create(MCity.get (Env.getCtx(), oi.getC_Location().getC_City_ID()), I_W_C_City.class);
		if (city.getlbr_CityCode() > 0)
			return "" + city.getlbr_CityCode();
		//
		return null;
	}	//	getCityCode
	
	/**
	 * 	Pega o código do estado
	 * 	
	 * 	@param oi
	 * 	@return	region code
	 */
	public static int getRegionCode (MOrgInfo oi)
	{
		String result = getCityCode(oi);
		//
		try
		{
			if (result != null && result.length() > 2)
				return Integer.parseInt (result.substring(0, 2));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//
		return 0;
	}	//	getRegionCode
}	//	NFeUtil
