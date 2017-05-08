package a;

import java.io.IOException;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

public class AnalogInputDevice {
//	private static final short ADC_CHANNEL_COUNT = 4;
	private static final byte POTENTIOMETER_CHANNEL = 0;
	private static final byte LDR_CHANNEL = 1;
	private static final int MIN_LDR_VAL = 0;
	private static final int MAX_LDR_VAL = 680;
	private static final int MIN_POTENTIOMETER_VAL = 0;
	private static final int MAX_POTENTIOMETER_VAL = 1024;
	
	private SpiDevice spi;
	
	/**
	 * Uses the default SPI channel of CS0
	 */
	public AnalogInputDevice() throws IOException  {
		this(SpiChannel.CS0);
	}
	
	public AnalogInputDevice(SpiChannel channel) throws IOException {
        spi = SpiFactory.getInstance(channel,
                SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0
	}
	
	public double sampleLDRNormalized() throws IOException {
		return ((double)getConversionValue(spi, LDR_CHANNEL) - MIN_LDR_VAL) / MAX_LDR_VAL;
	}
	
	public double samplePotentiometerNormalized() throws IOException {
		return ((double)getConversionValue(spi, POTENTIOMETER_CHANNEL) - MIN_POTENTIOMETER_VAL) / MAX_POTENTIOMETER_VAL;
	}
	
	
    // Obtained from https://github.com/Pi4J/pi4j/blob/master/pi4j-example/src/main/java/SpiExample.java
    /**
     * Communicate to the ADC chip via SPI to get single-ended conversion value for a specified channel.
     * @param channel analog input channel on ADC chip
     * @return conversion value for specified analog input channel
     * @throws IOException
     */
    private static int getConversionValue(SpiDevice spi, short channel) throws IOException {

        // create a data buffer and initialize a conversion request payload
        byte data[] = new byte[] {
                (byte) 0b00000001,                              // first byte, start bit
                (byte)(0b10000000 |( ((channel & 7) << 4))),    // second byte transmitted -> (SGL/DIF = 1, D2=D1=D0=0)
                (byte) 0b00000000                               // third byte transmitted....don't care
        };

        // send conversion request to ADC chip via SPI channel
        byte[] result = spi.write(data);

        // calculate and return conversion value from result bytes
        int value = (result[1]<< 8) & 0b1100000000; //merge data[1] & data[2] to get 10-bit result
        value |=  (result[2] & 0xff);
        return value;
    }
	
}
