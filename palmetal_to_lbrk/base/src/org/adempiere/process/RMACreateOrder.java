/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/

package org.adempiere.process;

import java.util.Map;

import org.adempierelbr.util.NewTaxBR;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MRMA;
import org.compiere.model.MRMALine;
import org.compiere.process.SvrProcess;

/**
 *  Creates Sales Order from RMA document
 *
 *  @author Ashley Ramdass
 */

public class RMACreateOrder extends SvrProcess
{
    @Override
    protected void prepare()
    {
        
    }
    
    @Override
    protected String doIt() throws Exception
    {
        int rmaId = getRecord_ID();
        
        // Load RMA
        MRMA rma = new MRMA(getCtx(), rmaId, get_TrxName());
        
        // Load Original Order
        MOrder originalOrder = rma.getOriginalOrder();
        
        if (rma.get_ID() == 0)
        {
            throw new Exception("No RMA defined");
        }
        
        if (originalOrder == null)
        {
            throw new Exception("Could not load the original order");
        }
        
        // Create new order and set the different values based on original order/RMA doc
        MOrder order = new MOrder(getCtx(), 0, get_TrxName());
        order.setAD_Org_ID(rma.getAD_Org_ID());
        order.setC_BPartner_ID(originalOrder.getC_BPartner_ID());
        order.setC_BPartner_Location_ID(originalOrder.getC_BPartner_Location_ID());
        order.setAD_User_ID(originalOrder.getAD_User_ID());
        order.setBill_BPartner_ID(originalOrder.getBill_BPartner_ID());
        order.setBill_Location_ID(originalOrder.getBill_Location_ID());
        order.setBill_User_ID(originalOrder.getBill_User_ID());
        order.setSalesRep_ID(rma.getSalesRep_ID());
        order.setM_PriceList_ID(originalOrder.getM_PriceList_ID());
        order.setM_Warehouse_ID(originalOrder.getM_Warehouse_ID());
        order.setC_DocTypeTarget_ID(rma.getC_DocType_ID());	//	Tipo de Doc Devolução
        order.setC_BPartner_Location_ID(originalOrder.getC_BPartner_Location_ID());
        order.setBill_BPartner_ID(originalOrder.getBill_BPartner_ID());
        order.setBill_Location_ID(originalOrder.getBill_Location_ID());
        order.setC_PaymentTerm_ID(originalOrder.getC_PaymentTerm_ID());
        order.setDeliveryRule(MOrder.DELIVERYRULE_Manual);
        order.set_CustomColumn("lbr_TransactionType", originalOrder.get_Value("lbr_TransactionType"));
        //
        if (!order.save())
        {
            throw new IllegalStateException("Could not create order");
        }
        
        MRMALine lines[] = rma.getLines(true);
                
        for (MRMALine line : lines)
        {
            if (line.getShipLine() != null && line.getShipLine().getC_OrderLine_ID() != 0)
            {
                // Create order lines if the RMA Doc line has a shipment line 
                MOrderLine orderLine = new MOrderLine(order);
                MOrderLine originalOLine = new MOrderLine(getCtx(), line.getShipLine().getC_OrderLine_ID(), null);
                orderLine.setAD_Org_ID(line.getAD_Org_ID());
                orderLine.setM_Product_ID(originalOLine.getM_Product_ID());
                orderLine.setM_AttributeSetInstance_ID(line.getShipLine().getM_AttributeSetInstance_ID());
                orderLine.setC_UOM_ID(originalOLine.getC_UOM_ID());
                orderLine.setC_Tax_ID(originalOLine.getC_Tax_ID());
                orderLine.setM_Warehouse_ID(originalOLine.getM_Warehouse_ID());
                orderLine.setC_Currency_ID(originalOLine.getC_Currency_ID());
                orderLine.setQty(line.getQty());
                orderLine.setC_Project_ID(originalOLine.getC_Project_ID());
                orderLine.setC_Activity_ID(originalOLine.getC_Activity_ID());
                orderLine.setC_Campaign_ID(originalOLine.getC_Campaign_ID());
                orderLine.setPrice();
                orderLine.setPrice(line.getAmt());
                //BEGIN tosello @faire
                Map<String,Integer> lbr = NewTaxBR.getTaxes(orderLine.getM_Product_ID(), orderLine.getC_Order_ID()
                			, orderLine.getC_OrderLine_ID(), order.getAD_Org_ID(), order.getC_BPartner_ID(),get_TrxName());
                orderLine.set_CustomColumn("LBR_LegalMessage_ID", lbr.get("LBR_LegalMessage_ID"));
                orderLine.set_CustomColumn("LBR_CFOP_ID", lbr.get("LBR_CFOP_ID"));
                orderLine.set_CustomColumn("LBR_Tax_ID", lbr.get("LBR_Tax_ID"));
                //END
                
                if (!orderLine.save())
                {
                    throw new IllegalStateException("Could not create Order Line");
                }
            }
        }
        
        rma.setC_Order_ID(order.getC_Order_ID());
        if (!rma.save())
        {
            throw new IllegalStateException("Could not update RMA document");
        }
        
        commit();
        
        return "Order Created: " + order.getDocumentNo();
    }

}
