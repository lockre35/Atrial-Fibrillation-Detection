rng default;
close all;
noisyECG = Volt3; % noisy waveform
t = 1:length(noisyECG);% time vector

figure(1);
plot(t,noisyECG)
title('Noisy Signal with Trend')
xlabel('Samples');
ylabel('Voltage(mV)')
legend('Noisy ECG Signal')
grid on

[p,s,mu] = polyfit((1:numel(noisyECG))',noisyECG,6);
f_y = polyval(p,(1:numel(noisyECG))',[],mu);

ECG_data = noisyECG - f_y;        % Detrend data

% Fs = 1000;
% FnormL = 2/(Fs/2); 
% dl = designfilt('lowpassfir','FilterOrder',70,'CutoffFrequency',FnormL);
% Dl = mean(grpdelay(dl));
% ECGlowpass = filter(dl,[ECG_data; zeros(Dl,1)]);
% 
% figure(8);
% plot(ECGlowpass)

smoothECG = sgolayfilt(ECG_data,7,21);
figure(2)
plot(t,noisyECG)
figure(3)
plot(t,noisyECG,'r'); grid on
xlabel('time'); ylabel('Voltage(mV)');
legend('Detrended  Signal')
title('Detrending Noisy ECG Signal')

%R-Wave
[~,locs_Rwave] = findpeaks(ECG_data,'MinPeakHeight',0.4,'MinPeakDistance',10);

figure(4);
hold on
plot(t,ECG_data);
plot(locs_Rwave,smoothECG(locs_Rwave),'rv','MarkerFaceColor','r');
grid on
title('Peaks Detected with Noise Reduction and Detrending')
%title('Savitzky-Golay Noise Reduction with Detrending')
xlabel('Samples'); ylabel('Voltage(mV)')
ax = axis; axis([0 3850 -1 2.0])
legend('Smooth ECG signal','R-wave');

n = 1;
locs_lastBeat = locs_Rwave(n);
locs_newBeat = locs_Rwave(n);
new_beatSpacing = locs_newBeat;
average_beatSpacing = (new_beatSpacing);

win = 6;

%Afib detection algorithm
while(n < win)
  n = n+1;
  locs_newBeat = locs_Rwave(n);
  new_beatSpacing = locs_newBeat - locs_lastBeat;
  beatSpacingArray(n-1) = new_beatSpacing;
  display(average_beatSpacing);display(new_beatSpacing);
  average_beatSpacing = (average_beatSpacing + new_beatSpacing)/2;
  locs_lastBeat = locs_newBeat;
  
  signal_shortBeatError = 0.3*average_beatSpacing;
  signal_longBeatError = 1.6*average_beatSpacing;
  
end

val2 = length(beatSpacingArray);
x = beatSpacingArray;
xmean = mean(x)
for i = 1:val2
%     rmsx(i) = sqrt((1/win)*sum(((x(win*(i-1)+1:i*win)-mean(x(win*(i-1)+1:i*win)))).^2));
   rmsx(i) = sqrt(sum((x(i)-xmean).^2));

end

figure(5)
hist(x);
figure(6)
hist(rmsx);

Lags = 5;
y = autocorr(rmsx,Lags);

figure(7);
plot([1:1:Lags+1],y)


