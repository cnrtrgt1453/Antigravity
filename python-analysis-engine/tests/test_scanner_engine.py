import pytest
from unittest.mock import MagicMock, patch
from app.services.scanner_engine import ScannerEngine

class TestScannerEngine:
    
    @pytest.fixture
    def mock_dependencies(self):
        data_provider = MagicMock()
        strategy = MagicMock()
        reporter = MagicMock()
        return data_provider, strategy, reporter

    def test_scanner_engine_initialization(self, mock_dependencies):
        dp, st, rep = mock_dependencies
        engine = ScannerEngine(dp, st, rep)
        
        assert engine.data_provider == dp
        assert engine.strategy == st
        assert engine.reporter == rep

    @patch('app.services.scanner_engine.SessionLocal')
    def test_get_active_instruments(self, mock_session, mock_dependencies):
        dp, st, rep = mock_dependencies
        engine = ScannerEngine(dp, st, rep)
        
        # Mock DB results
        mock_db = MagicMock()
        mock_session.return_value = mock_db
        
        mock_inst1 = MagicMock()
        mock_inst1.symbol = "THYAO"
        mock_inst2 = MagicMock()
        mock_inst2.symbol = "ASELS"
        
        mock_db.query.return_value.filter.return_value.all.return_value = [mock_inst1, mock_inst2]
        
        symbols = engine.get_active_instruments()
        
        assert symbols == ["THYAO", "ASELS"]
        mock_db.close.assert_called_once()

    def test_process_single_instrument(self, mock_dependencies):
        dp, st, rep = mock_dependencies
        engine = ScannerEngine(dp, st, rep)
        
        dp.fetch_historical_data.return_value = "mock_df"
        st.analyze.return_value = {"ticker": "THYAO", "signal": "GOLDEN_CROSS", "cross_date": "2024-01-01"}
        
        result = engine._process_single_instrument("THYAO")
        
        assert result["ticker"] == "THYAO"
        dp.fetch_historical_data.assert_called_with(ticker="THYAO")
        st.analyze.assert_called_with(df="mock_df", ticker="THYAO")

    @patch.object(ScannerEngine, 'get_active_instruments')
    @patch.object(ScannerEngine, '_process_single_instrument')
    def test_run_scan(self, mock_process, mock_get_inst, mock_dependencies):
        dp, st, rep = mock_dependencies
        engine = ScannerEngine(dp, st, rep)
        
        mock_get_inst.return_value = ["THYAO", "ASELS"]
        mock_process.side_effect = [
            {"ticker": "THYAO", "signal": "GOLDEN_CROSS", "cross_date": "2024-01-01"},
            {"ticker": "ASELS", "signal": "NO_SIGNAL"}
        ]
        
        engine.run_scan()
        
        assert rep.report.called
        # Check if reporter was called with a list containing 2 results
        args, _ = rep.report.call_args
        assert len(args[0]) == 2
